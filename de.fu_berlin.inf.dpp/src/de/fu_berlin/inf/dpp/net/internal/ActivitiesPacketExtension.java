/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universitaet Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package de.fu_berlin.inf.dpp.net.internal;

import java.text.MessageFormat;
import java.util.List;

import org.jivesoftware.smack.packet.PacketExtension;

import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.net.TimedActivity;
import de.fu_berlin.inf.dpp.project.ActivityRegistry;

public class ActivitiesPacketExtension implements PacketExtension {
    public static final String NAMESPACE = "de.fu_berlin.inf.dpp";

    public static final String SESSION_ID = "sessionID";

    public static final String ELEMENT = "activities";

    public static final String TEXT_CHANGE_TAG = "edit";

    private List<TimedActivity> activities;

    MessageFormat textChangeFormat = new MessageFormat(
            "<{0} offset=\"{1}\" replace=\"{2}\">{3}</{4}>"); // TODO extract

    private String sessionID;

    // into

    // consts

    public ActivitiesPacketExtension() {
    }

    public ActivitiesPacketExtension(String sessionID,
            List<TimedActivity> activities) {
        this.sessionID = sessionID;
        setActivities(activities);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jivesoftware.smack.packet.PacketExtension#getElementName()
     */
    public String getElementName() {
        return ActivitiesPacketExtension.ELEMENT;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jivesoftware.smack.packet.PacketExtension#getNamespace()
     */
    public String getNamespace() {
        return ActivitiesPacketExtension.NAMESPACE;
    }

    public void setActivities(List<TimedActivity> activities) {
        this.activities = activities;
    }

    public List<TimedActivity> getActivities() {
        return this.activities;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jivesoftware.smack.packet.PacketExtension#toXML()
     */
    public String toXML() {
        if (this.activities.size() == 0) {
            return "";
        }

        StringBuffer buf = new StringBuffer();
        buf.append("<").append(getElementName());
        buf.append(" xmlns=\"").append(getNamespace()).append("\">");

        buf.append(sessionIdToXML());

        int firstTimestamp = this.activities.get(0).getTimestamp();
        buf.append("<timestamp>").append(firstTimestamp).append("</timestamp>");

        ActivityRegistry activityRegistry = ActivityRegistry.getDefault();
        for (TimedActivity timedActivity : this.activities) {
            IActivity activity = timedActivity.getActivity();
            buf.append(activityRegistry.toXML(activity));
        }

        buf.append("</").append(getElementName()).append(">");
        return buf.toString();
    }

    private String sessionIdToXML() {
        return "<" + ActivitiesPacketExtension.SESSION_ID + ">" + sessionID
                + "</" + ActivitiesPacketExtension.SESSION_ID + ">";
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ActivitiesPacketExtension) {
            ActivitiesPacketExtension other = (ActivitiesPacketExtension) obj;

            return this.activities.equals(other.getActivities());
        }

        return false;
    }

    public String getSessionID() {
        return sessionID;
    }
}
