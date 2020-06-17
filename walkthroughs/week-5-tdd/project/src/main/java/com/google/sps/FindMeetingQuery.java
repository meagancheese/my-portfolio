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
  
  private Collection<TimeRange> timeOptions = new ArrayList<TimeRange>();
  
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    timeOptions.clear();
    
    if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
      return Arrays.asList();
    }
    
    Set<String> optionalRequestAttendees = new HashSet<>();
    optionalRequestAttendees.addAll(request.getOptionalAttendees());
    
    Set<String> mandatoryRequestAttendees = new HashSet<>();
    mandatoryRequestAttendees.addAll(request.getAttendees());
    
    Set<String> allRequestAttendees = new HashSet<>();
    allRequestAttendees.addAll(optionalRequestAttendees);
    allRequestAttendees.addAll(mandatoryRequestAttendees);
    
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
    
    List<Event> eventsList = new ArrayList<Event>();
    eventsList.addAll(events);
    Collections.sort(eventsList, Event.ORDER_BY_START);
    
    List<Event> optionalAttendeeEvents = new ArrayList<Event>();
    for (Event event : eventsList) {
      if (optionalAttendeesOnly(event, optionalRequestAttendees)) {
        optionalAttendeeEvents.add(event);
      }
    }
    
    List<Event> mandatoryAttendeeEvents = eventsList.removeAll(optionalAttendeeEvents);
    
    TimeRange firstEventTime = eventsList.get(0).getWhen();
    TimeRange lastEventTime = eventsList.get(eventsList.size() - 1).getWhen();
    boolean lastEventSkipped = false;
    if (TimeRange.START_OF_DAY != firstEventTime.start()) {
      addTimeOption(TimeRange.START_OF_DAY, firstEventTime.start());
    }
    for (int i = 1; i < eventsList.size(); i++) {
      TimeRange currentEventTime = eventsList.get(i - 1).getWhen();
      TimeRange nextEventTime = eventsList.get(i).getWhen();
      TimeRange skipNextTime = null; 
      if (i + 1 < eventsList.size()) {
        skipNextTime = eventsList.get(i + 1).getWhen();
      }
      if (backToBack(currentEventTime, nextEventTime)) {
        // When two events are back-to-back, they can be considered as one long event
        // and so we will add a meeting time option only after the next event.
        continue;
      }
      if (!currentEventTime.overlaps(nextEventTime)) {
        addTimeOption(currentEventTime.end(), nextEventTime.start());
        continue;
      }
      if (currentEventTime.contains(nextEventTime)) {
        if (i + 1 < eventsList.size()) {
          addTimeOption(currentEventTime.end(), skipNextTime.start());
        } else {
          lastEventSkipped = true;
          if (currentEventTime.end() - 1 != TimeRange.END_OF_DAY) {
            addTimeOption(currentEventTime.end(), TimeRange.END_OF_DAY);
          }
        }
      } 
       
    }
    if ((lastEventTime.end() - 1 != TimeRange.END_OF_DAY) && !lastEventSkipped) {
      addTimeOption(lastEventTime.end(), TimeRange.END_OF_DAY);
    }
    
    Collection<TimeRange> tooShortTimeOptions = new ArrayList<TimeRange>();
    for (TimeRange meetingOption : timeOptions) {
      if (meetingOption.duration() < request.getDuration()) {
        tooShortTimeOptions.add(meetingOption);
      }
    }
    timeOptions.removeAll(tooShortTimeOptions);
    
    return timeOptions;
  }
  
  private void addTimeOption(int start, int end) {
    boolean inclusive = end == TimeRange.END_OF_DAY;
    timeOptions.add(TimeRange.fromStartEnd(start, end, inclusive));
  }
  
  private boolean backToBack(TimeRange firstTimeRange, TimeRange secondTimeRange) {
    return firstTimeRange.end() == secondTimeRange.start();
  }
  
  private boolean optionalAttendeesOnly(Event event, Set<String> optionalAttendees) {
    int optionalAttendeeCount = 0;
    for (String attendee : optionalAttendees) {
      if (event.getAttendees().contains(attendee)) {
        optionalAttendeeCount++;
      }
    }
    if (event.getAttendees().size() == optionalAttendeeCount) {
      return true;
    }
    return false;
  }
}
