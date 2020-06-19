// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class FindMeetingQuery {
  
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
      return Arrays.asList();
    }
    
    Set<String> optionalRequestAttendees = new HashSet<String>(request.getOptionalAttendees());
    Set<String> mandatoryRequestAttendees = new HashSet<String>(request.getAttendees());
    Set<String> allRequestAttendees = combineAttendeeSets(optionalRequestAttendees, mandatoryRequestAttendees);
    
    if (allRequestAttendees.isEmpty() || events.isEmpty()) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }
    
    Set<String> allEventAttendees = new HashSet<>();
    for (Event event : events) {
      allEventAttendees.addAll(event.getAttendees());
    }
    
    int attendeesWithNoEvents = 0;
    for (String attendee : allRequestAttendees) {
      if (!allEventAttendees.contains(attendee)) {
        attendeesWithNoEvents++;
      }
    }
    if (attendeesWithNoEvents == allRequestAttendees.size()) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }
    
    List<Event> allEvents = new ArrayList<Event>(events);
    sortByStart(allEvents);
  
    Collection<TimeRange> everyoneTimeOptions = getAllPossibleTimes(allEvents, request);
    
    if (!everyoneTimeOptions.isEmpty()) {
      return everyoneTimeOptions;
    }
    
    List<Event> mandatoryAttendeeEvents = new ArrayList<Event>();
    List<Event> optionalAttendeeEvents = new ArrayList<Event>();
    for (Event event : allEvents) {
      if (!optionalAttendeesOnly(event, mandatoryRequestAttendees)) {
        mandatoryAttendeeEvents.add(event);
      } else {
        optionalAttendeeEvents.add(event);
      }
    }
    sortByStart(mandatoryAttendeeEvents);
    sortByStart(optionalAttendeeEvents);
    
    if (mandatoryAttendeeEvents.isEmpty()) {
      return everyoneTimeOptions;
    }
    
    ArrayList<TimeRange> mandatoryAttendeeTimeOptions = getAllPossibleTimes(mandatoryAttendeeEvents, request);
    
    List<ArrayList<TimeRange>> possibleTimeOptions = new ArrayList<ArrayList<TimeRange>>();
    possibleTimeOptions.add(mandatoryAttendeeTimeOptions);
    
    List<ArrayList<Event>> optionalAttendeeEventsCollection = new ArrayList<ArrayList<Event>>();
    
    // Sorts events according to their optional attendees, making a separate ArrayList for each optional
    // attendee's events.
    for (String optionalAttendee : optionalRequestAttendees) {
      ArrayList<Event> thisOptionalAttendeeEvents = new ArrayList<Event>();
      for (Event event : optionalAttendeeEvents) {
        if (event.getAttendees().contains(optionalAttendee)) {
          thisOptionalAttendeeEvents.add(event);
        }
      }
      sortByStart(thisOptionalAttendeeEvents);
      optionalAttendeeEventsCollection.add(thisOptionalAttendeeEvents);
    }
    
    possibleTimeOptions = addOptionalAttendees(
      possibleTimeOptions, optionalAttendeeEventsCollection, mandatoryAttendeeEvents, request
    );
    
    return tieBreaker(possibleTimeOptions);
  }
  
  private Set<String> combineAttendeeSets(Collection<String> attendeeSetOne, Collection<String> attendeeSetTwo) {
    Set<String> combinedSet = new HashSet<String>(attendeeSetOne);
    combinedSet.addAll(attendeeSetTwo);
    return combinedSet;
  }
  
  private void sortByStart(List<Event> eventsList) {
    Collections.sort(eventsList, Event.ORDER_BY_START);
  }
  
  private boolean optionalAttendeesOnly(Event event, Set<String> mandatoryAttendees) {
    for (String attendee : mandatoryAttendees) {
      if (event.getAttendees().contains(attendee)) {
        return false;
      }
    }
    return true;
  }
  
  private ArrayList<TimeRange> getAllPossibleTimes(List<Event> eventsList, MeetingRequest request) {
    ArrayList<TimeRange> timeOptions = new ArrayList<TimeRange>();
    TimeRange firstEventTime = eventsList.get(0).getWhen();
    TimeRange lastEventTime = eventsList.get(eventsList.size() - 1).getWhen();
    boolean lastEventSkipped = false;
    if (TimeRange.START_OF_DAY != firstEventTime.start()) {
      addTimeOption(timeOptions, TimeRange.START_OF_DAY, firstEventTime.start());
    }
    TimeRange currentEventTime = firstEventTime;
    for (int i = 1; i < eventsList.size(); i++) {
      TimeRange nextEventTime = eventsList.get(i).getWhen();
      if (backToBack(currentEventTime, nextEventTime)) {
        // When two events are back-to-back, they can be considered as one long event
        // and so we will add a meeting time option only after the next event.
        currentEventTime = nextEventTime;
        continue;
      }
      if (!currentEventTime.overlaps(nextEventTime)) {
        addTimeOption(timeOptions, currentEventTime.end(), nextEventTime.start());
        currentEventTime = nextEventTime;
        continue;
      }
      if (currentEventTime.contains(nextEventTime)) {
        continue;
      }
      // When two events overlap but currentEventTime does not contain nextEventTime
      currentEventTime = nextEventTime;
    }
    if ((currentEventTime.end() - 1) != TimeRange.END_OF_DAY) {
      addTimeOption(timeOptions, currentEventTime.end(), TimeRange.END_OF_DAY);
    }
    return removeTooSmallTimes(timeOptions, request);
  }
  
  private void addTimeOption(Collection<TimeRange> timeOptions, int start, int end) {
    boolean inclusive = end == TimeRange.END_OF_DAY;
    timeOptions.add(TimeRange.fromStartEnd(start, end, inclusive));
  }
  
  private boolean backToBack(TimeRange firstTimeRange, TimeRange secondTimeRange) {
    return firstTimeRange.end() == secondTimeRange.start();
  }
  
  private ArrayList<TimeRange> removeTooSmallTimes(ArrayList<TimeRange> timeOptions, MeetingRequest request) {
    ArrayList<TimeRange> tooShortTimeOptions = new ArrayList<TimeRange>();
    for (TimeRange meetingOption : timeOptions) {
      if (meetingOption.duration() < request.getDuration()) {
        tooShortTimeOptions.add(meetingOption);
      }
    }
    timeOptions.removeAll(tooShortTimeOptions);
    return timeOptions;
  }
  
  private List<ArrayList<TimeRange>> addOptionalAttendees(List<ArrayList<TimeRange>> currentOptions, 
    List<ArrayList<Event>> tryingToAdd, List<Event> existingEvents, MeetingRequest request) {
    if (tryingToAdd.isEmpty()) {
      return currentOptions;
    }
    List<ArrayList<TimeRange>> bestOptions = new ArrayList<ArrayList<TimeRange>>();
    for (ArrayList<Event> optionToAdd : tryingToAdd) {
      List<ArrayList<Event>> updatedOptionsToAdd = new ArrayList<ArrayList<Event>>();
      updatedOptionsToAdd.addAll(tryingToAdd);
      updatedOptionsToAdd.remove(optionToAdd);
      List<Event> newEventList = new ArrayList<Event>(existingEvents);
      newEventList.addAll(optionToAdd);
      sortByStart(newEventList);
      ArrayList<TimeRange> newTimeOptions = getAllPossibleTimes(newEventList, request);
      if (!newTimeOptions.isEmpty()) {
        addIfDoesNotExist(bestOptions, addOptionalAttendees(Arrays.asList(newTimeOptions), updatedOptionsToAdd, newEventList, request));
      }
    }
    if (bestOptions.isEmpty()) {
      return currentOptions;
    }
    return bestOptions;
  }
  
  private void addIfDoesNotExist(List<ArrayList<TimeRange>> timeCollection, List<ArrayList<TimeRange>> timeOptionsToAdd) {
    if (!timeCollection.contains(timeOptionsToAdd)) {
      timeCollection.addAll(timeOptionsToAdd);
    }
    return;
  }
  
  private Collection<TimeRange> tieBreaker(List<ArrayList<TimeRange>> timeCollection) {
    if (timeCollection.isEmpty()) {
      return Arrays.asList();
    }
    ArrayList<TimeRange> bestOption = timeCollection.get(0);
    for (int i = 1; i < timeCollection.size(); i++) {
      ArrayList<TimeRange> currentContender = timeCollection.get(i);
      // The Option that gives the widest time range (greatest total duration) is the best
      if (getTotalDuration(currentContender) > getTotalDuration(bestOption)) {
        bestOption = currentContender;
      } else if (getTotalDuration(currentContender) == getTotalDuration(bestOption)) {
        // If two options have the same total duration, we will pick the option that has
        // the earliest start option
        if (currentContender.get(0).start() < bestOption.get(0).start()) {
        bestOption = currentContender;
        }
      }
    }
    return bestOption;
  }
  
  private int getTotalDuration(Collection<TimeRange> timeOptions) {
    int totalDuration = 0;
    for (TimeRange time : timeOptions) {
      totalDuration += time.duration();
    }
    return totalDuration;
  }
}
