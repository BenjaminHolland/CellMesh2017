/*
 * Copyright (c) 2017 Vladimir L. Shabanov <virlof@gmail.com>
 *
 * Licensed under the Underdark License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://underdark.io/LICENSE.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cellmesh.app.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

import io.underdark.transport.Link;


public interface INodeListener {
    /*
    Called when two members exchange name data.
    UI MUST update to reflect new Link/Name associations.
     */
    void onNamesUpdated(Map<Long, String> names);
    /*
    Called when a member joins our swarm.
    UI MAY display a notification acknowledging the event.
    UI MAY keep a friends list. This is not a priority goal.
     */
    void onConnected(Set<Long> readOnlyIds, Long newId);
    /*
    Called when a member leaves our swarm.
    UI MAY display a notification acknloging the event.
    UI MAY keep a friends list. This is not a priority goal.
     */
    void onDisconnected(Set<Long> readOnlyIds,Long oldLinkId);

    /*
    Called when we receive data from a member.
    UI MUST append the message to a message log with the name
    currently associated with the link id.
     */
    void onDataReceived(String newMessage, Long fromLinkId);
    /*
    Called when we send data to the swarm
    UI MUST append the message to a message log with the users nickname.
    NOTE: This method should be used to update the UI, rather than having
    the UI update the message log directly before or after a call to
    Node.broadcastMessage. This allows optimizations behind the scenes.
     */
    void onDataSent(String newMessage, Long fromLinkId);
    /*
    Called when we receive an emergency notification from another link.
    The UI MUST display a message acknologing the event.
     */
    void onEmergency(Long fromLinkId);
}
