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
    
    List<Event> allEvents = new ArrayList<Event>();
    allEvents.addAll(events);
    sortByStart(allEvents);
    
    List<Event> mandatoryAttendeeEvents = new ArrayList<Event>();
    for (Event event : allEvents) {
      if (!optionalAttendeesOnly(event, mandatoryRequestAttendees)) {
        mandatoryAttendeeEvents.add(event);
      }
    }
    sortByStart(mandatoryAttendeeEvents);
  
    Collection<TimeRange> everyoneTimeOptions = getAllPossibleTimes(allEvents, request);
    
    if (everyoneTimeOptions.isEmpty() && !mandatoryAttendeeEvents.isEmpty()) {
      return getAllPossibleTimes(mandatoryAttendeeEvents, request);
    }
   
    return everyoneTimeOptions;
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
  
  private Collection<TimeRange> getAllPossibleTimes(List<Event> eventsList, MeetingRequest request) {
    Collection<TimeRange> timeOptions = new ArrayList<TimeRange>();
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
  
  private Collection<TimeRange> removeTooSmallTimes(Collection<TimeRange> timeOptions, MeetingRequest request) {
    Collection<TimeRange> tooShortTimeOptions = new ArrayList<TimeRange>();
    for (TimeRange meetingOption : timeOptions) {
      if (meetingOption.duration() < request.getDuration()) {
        tooShortTimeOptions.add(meetingOption);
      }
    }
    timeOptions.removeAll(tooShortTimeOptions);
    return timeOptions;
  }
}
