/*

 Copyright 2012 VU Medical Center Amsterdam

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 */
package nl.vumc.trait.oc.types;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.openclinica.ws.beans.StudySubjectWithEventsType;

/**
 * Simple representation of a Study Subject
 *
 * @author Arjan van der Velde (a.vandervelde (at) xs4all.nl)
 */
public class StudySubject {

    private Logger logger = LogManager.getLogger(StudySubject.class);
    /**
     * XML data type factory
     */
    private DatatypeFactory dataTypeFactory;
    /**
     * Study Subject OID
     */
    private String studySubjectOID;
    /**
     * Study Subject Person ID a.k.a. unique identifier
     */
    private String personID;
    /**
     * Study Subject Label (== study number)
     */
    private String studySubjectLabel;
    /**
     * Study Subject gender
     */
    private String sex;
    /**
     * Study Subject date of birth
     */
    private XMLGregorianCalendar dateOfBirth;
    /**
     * Study Subject date of registration
     */
    private XMLGregorianCalendar dateOfRegistration;
    /**
     * Events scheduled for this Study Subject
     */
    private ArrayList<ScheduledEvent> scheduledEvents;
    /**
     * The Study this is a subject for
     */
    private Study study;
    /**
     * The site of the study with which the subject is associated
     */
    private String siteOID;

    /**
     * Create a study subject for a given Study
     *
     * @param study The study this is a subject for
     * @throws DatatypeConfigurationException
     */
    public StudySubject(Study study) {
        try {
            dataTypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException dce) {
            throw new IllegalStateException("Unable to create dataTypeFactory. Cause:\n", dce);
        }
        dateOfRegistration = dataTypeFactory.newXMLGregorianCalendar(new GregorianCalendar());
        dateOfRegistration.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
        this.study = study;
    }

    /**
     * Create a study subject from OC StudySubjectWithEventsType object, for a
     * given Study
     *
     * @param study the study this will be a subject for
     * @param subject the subject as a OC StudySubjectWithEventsType object
     * @throws DatatypeConfigurationException
     */
    public StudySubject(Study study, StudySubjectWithEventsType subject) {
        this(study);
        updateStudySubject(subject);
    }

    /**
     * Update this study subject from a OC StudySubjectWithEventsType object
     *
     * @param subject the OC StudySubjectWithEventsType to use
     */
    public final void updateStudySubject(StudySubjectWithEventsType subject) {
        this.personID = subject.getSubject().getUniqueIdentifier();
        this.sex = subject.getSubject().getGender().value();
        this.studySubjectLabel = subject.getLabel();
        this.dateOfBirth = subject.getSubject().getDateOfBirth();
        this.dateOfRegistration = subject.getEnrollmentDate();
    }

    /**
     * Get PersonID
     *
     * @return personID
     */
    public String getPersonID() {
        return personID;
    }

    /**
     * Set personID
     *
     * @param personID personID
     */
    public void setPersonID(String personID) {
        this.personID = personID;
    }

    /**
     * Get studySubjectLabel
     *
     * @return studySubjectLabel
     */
    public String getStudySubjectLabel() {
        return studySubjectLabel;
    }

    /**
     * Set studySubjectLabel
     *
     * @param studySubjectLabel studySubjectLabel
     */
    public void setStudySubjectLabel(String studySubjectLabel) {
        this.studySubjectLabel = studySubjectLabel;
    }

    /**
     * Get sex!
     *
     * @return sex
     */
    public String getSex() {
        return sex;
    }

    /**
     * Set sex
     *
     * @param sex sex
     */
    public void setSex(String sex) {
        this.sex = sex;
    }

    /**
     * Get date of birth
     *
     * @return dateOfBirth
     */
    public XMLGregorianCalendar getDateOfBirth() {
        return dateOfBirth;
    }

    /**
     * Set dateOfBirth
     *
     * @param dateOfBirth dateOfBirth
     */
    public void setDateOfBirth(XMLGregorianCalendar dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        this.dateOfBirth.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
    }

    /**
     * Set dateOfBirth from String
     *
     * @param dateOfBirth dateOfBirth as String (yyyy-mm-dd)
     */
    public void setDateOfBirth(String dateOfBirth) {
        logger.debug("Setting DOB : '" + dateOfBirth + "'");
        setDateOfBirth(dataTypeFactory.newXMLGregorianCalendar(dateOfBirth));
    }

    /**
     * Get registration date
     *
     * @return dateOfRegistration
     */
    public XMLGregorianCalendar getDateOfRegistration() {
        return dateOfRegistration;
    }

    /**
     * Set registration date
     *
     * @param dateOfRegistration dateOfRegistration
     */
    public void setDateOfRegistration(XMLGregorianCalendar dateOfRegistration) {
        this.dateOfRegistration = dateOfRegistration;
        this.dateOfRegistration.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
    }

    /**
     * Set registration date from String
     *
     * @param dateOfRegistration dateOfRegistration as String (yyyy-mm-dd)
     */
    public void setDateOfRegistration(String dateOfRegistration) {
        setDateOfRegistration(dataTypeFactory.newXMLGregorianCalendar(dateOfRegistration));
    }

    /**
     * Get events scheduled for this subject
     *
     * @return list of events
     */
    public ArrayList<ScheduledEvent> getScheduledEvents() {
        if (scheduledEvents == null) {
            scheduledEvents = new ArrayList<ScheduledEvent>();
        }
        return scheduledEvents;
    }

    /**
     * Set schedules events list
     *
     * @param scheduledEvents Scheduled events
     */
    public void setScheduledEvents(ArrayList<ScheduledEvent> scheduledEvents) {
        this.scheduledEvents = scheduledEvents;
    }

    /**
     * Get subject OID
     *
     * @return the studySubjectOID
     */
    public String getStudySubjectOID() {
        return studySubjectOID;
    }

    /**
     * Set subject OID
     *
     * @param studySubjectOID the studySubjectOID to set
     */
    public void setStudySubjectOID(String studySubjectOID) {
        this.studySubjectOID = studySubjectOID;
    }

    /**
     * Get study
     *
     * @return the study
     */
    public Study getStudy() {
        return study;
    }

    /**
     * Set study
     *
     * @param study the study to set
     */
    public void setStudy(Study study) {
        this.study = study;
    }

    public String getSiteOID() {
        return siteOID;
    }

    public void setSiteOID(String siteOID) {
        this.siteOID = siteOID;
    }

    /**
     * Check for presence of OID
     *
     * @return present or not (bool)
     */
    public boolean hasOID() {
        return studySubjectOID != null;
    }

    @Override
    public String toString() {
        return "StudySubject: OID: " + studySubjectOID + ", personID: " + personID + ", studySubjectLabel: "
                + studySubjectLabel + ", sex: " + sex + ", dateOfBirth: " + dateOfBirth + ", dateOfRegistration: "
                + dateOfRegistration + ", StudySubject Events: " + scheduledEvents;
    }

    @Override
    public int hashCode() {
        return (personID + studySubjectLabel).hashCode();
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (!(that instanceof StudySubject)) {
            return false;
        }
        StudySubject otherSubject = (StudySubject) that;
        return EqualsUtil.areEqual(this.personID, otherSubject.personID)
                && EqualsUtil.areEqual(this.studySubjectLabel, otherSubject.studySubjectLabel)
                && EqualsUtil.areEqual(this.study, otherSubject.study);
    }
}
