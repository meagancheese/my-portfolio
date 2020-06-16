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
    if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
      return Arrays.asList();
    }
    
    if (request.getAttendees().isEmpty() || events.isEmpty()) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }
    
    Set<String> allEventAttendees = new HashSet<>();
    for (Event event : events) {
      allEventAttendees.addAll(event.getAttendees());
    }
    Set<String> allRequestAttendees = new HashSet<>();
    allRequestAttendees.addAll(request.getAttendees());
    for (String attendee : allRequestAttendees) {
      if (!allEventAttendees.contains(attendee)) {
        return Arrays.asList(TimeRange.WHOLE_DAY);
      }
    }
    
    List<Event> eventsList = new ArrayList<Event>();
    eventsList.addAll(events);
    Collections.sort(eventsList, Event.ORDER_BY_START);
    boolean earlyEnd = false;
    if (TimeRange.START_OF_DAY != eventsList.get(0).getWhen().start()) {
      addTimeRange(TimeRange.START_OF_DAY, eventsList.get(0).getWhen().start());
    }
    for (int i = 1; i < eventsList.size(); i++) {
      if (eventsList.get(i - 1).getWhen().end() != eventsList.get(i).getWhen().start()) {
        if (!eventsList.get(i - 1).getWhen().overlaps(eventsList.get(i).getWhen())) {
          addTimeRange(eventsList.get(i - 1).getWhen().end(), eventsList.get(i).getWhen().start());
        }
        if (eventsList.get(i - 1).getWhen().contains(eventsList.get(i).getWhen())) {
          if (i + 1 < eventsList.size()) {
            addTimeRange(eventsList.get(i - 1).getWhen().end(), eventsList.get(i + 1).getWhen().start());
          } else {
            earlyEnd = true;
            if (eventsList.get(eventsList.size() - 1).getWhen().end() != TimeRange.END_OF_DAY) {
              addTimeRange(eventsList.get(i - 1).getWhen().end(), TimeRange.END_OF_DAY);
            }
          }
        } 
      } 
    }
    if ((eventsList.get(eventsList.size() - 1).getWhen().end() - 1 != TimeRange.END_OF_DAY) && !earlyEnd) {
      addTimeRange(eventsList.get(eventsList.size() - 1).getWhen().end(), TimeRange.END_OF_DAY);
    }
    
    for (TimeRange meetingOption : timeOptions) {
      if (meetingOption.duration() >= request.getDuration()) {
        return timeOptions;
      }
    }
    
    return Arrays.asList();
  }
  
  private void addTimeRange(int start, int end) {
    boolean inclusive;
    if (end == TimeRange.END_OF_DAY) {
      inclusive = true;
    } else {
      inclusive = false;
    }
    timeOptions.add(TimeRange.fromStartEnd(start, end, inclusive));
  }
}
