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
import java.util.List;

public final class FindMeetingQuery {
  
  Collection<TimeRange> timeOptions = new ArrayList<TimeRange>();
  
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    
    List<Event> eventsList = new ArrayList<Event>();
    eventsList.addAll(events);
    Collections.sort(eventsList, Event.ORDER_BY_START);
    
    //noOptionsForTooLongOfARequest
    if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
      return Arrays.asList();
    }
    
    //optionsForNoAttendees and noConflicts
    if (request.getAttendees().isEmpty() || events.isEmpty()) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }
    
    //eventSplitsRestriction
    //Will only consider one event for now
    if (events.size() == 1) {
      for (Event event : events) {
        addTimeRange(TimeRange.START_OF_DAY, event.getWhen().start());
        addTimeRange(event.getWhen().end(), TimeRange.END_OF_DAY);
      }
    }
    
    //everyAttendeeIsConsidered
    //For when two people each have a meeting for now
    if (events.size() == 2) {
      addTimeRange(TimeRange.START_OF_DAY, eventsList.get(0).getWhen().start());
      addTimeRange(eventsList.get(0).getWhen().end(), eventsList.get(1).getWhen().start());
      addTimeRange(eventsList.get(1).getWhen().end(), TimeRange.END_OF_DAY);
    }
    
    //overlappingEvents
    
    
    // throw new UnsupportedOperationException("TODO: Implement this method.");
    
    //Default for now, needed a return statement.
    return timeOptions;
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
