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
package nl.vumc.trait.oc.odm;

import java.util.Collection;
import java.util.HashMap;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import nl.vumc.trait.oc.connect.OCConnectorException;
import nl.vumc.trait.oc.connect.OCWebServices;
import nl.vumc.trait.oc.types.ScheduledEvent;
import nl.vumc.trait.oc.types.Study;
import nl.vumc.trait.oc.types.StudySubject;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.openclinica.ws.study.v1.ListAllResponse;
import org.openclinica.ws.studysubject.v1.IsStudySubjectResponse;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Clinical ODM with functionality to resolve non-OID ids from OpenClinica IDs
 *
 * @author Arjan van der Velde (a.vandervelde (at) xs4all.nl)
 *
 */
public class ClinicalODMResolver extends ClinicalODM {

    private Logger logger = LogManager.getLogger(ClinicalODMResolver.class);
    // TODO: too many constructors here :S
    /**
     * Attribute: Mirth:TranslateOID, indicates whether or not to translate a
     * certain element
     */
    private static final String ATTR_MIRTH_TRANSLATEOID = "Mirth:TranslateOID";
    // TODO change the code to use the ODM1.3 siteRef@LocationOID construction;
    // JR 12-06-2013
    /**
     * The site of a subject. Should be placed on the SubjectData element in the
     * ODM.
     */
    private static final String ATTR_MIRTH_SITE_IDENDIFIER = "Mirth:siteIdentifier";
    /**
     * Attribute: Mirth:Create, indicates whether or not to create a certain
     * element if non-existent
     */
    private static final String ATTR_MIRTH_CREATE = "Mirth:Create";
    /**
     * Attribute:
     * <code>Mirth:ScheduleOnly<code>, indicates that the event only has to be
     * scheduled and no data has to be uploaded. Located on the ODM element
     * <code>StudyEventData</code>
     */
    private static final String ATTR_MIRTH_SCHEDULE_ONLY = "Mirth:ScheduleOnly";
    /**
     * Attribute: OpenClinica, data of birth
     */
    private static final String ATTR_OC_DATEOFBIRTH = "OpenClinica:DateOfBirth";
    /**
     * Attribute: OpenClinica, registration date
     */
    private static final String ATTR_OC_DATEOFREGISTRATION = "OpenClinica:DateOfRegistration";
    /**
     * Attribute: OpenClinica, gender
     */
    private static final String ATTR_OC_SEX = "OpenClinica:Sex";
    /**
     * Attribute: SubjectKey (can be OID or label depending on
     * Mirth:TranslateOID)
     */
    private static final String ATTR_OC_SUBJECTKEY = "SubjectKey";
    /**
     * Attribute: OpenClinica, unique identifier: Person ID
     */
    private static final String ATTR_OC_PERSONID = "OpenClinica:UniqueIdentifier";
    /**
     * Attribute: ODM, Study Event OID
     */
    private static final String ATTR_STUDY_EVENT_OID = "StudyEventOID";
    /**
     * Attribute: OpenClinica, start date (of event)
     */
    private static final String ATTR_OC_START_DATE = "OpenClinica:StartDate";
    /**
     * Attribute: ODM, Study OID or label (can be OID or label depending on
     * Mirth:TranslateOID)
     */
    private static final String ATTR_STUDYOID = "StudyOID";
    // TODO: private final String ATTR_STUDY_EVENT_REPEAT_KEY = "StudyEventRepeatKey";
    /**
     * XPath Query: ClinicalData element, the root of OpenClinica loadable data
     */
    private static final String XPATH_CLINICAL_DATA = "/ODM/ClinicalData";
    /**
     * XPath Query: Subject Data -- Child(ren) of ClinicalData
     */
    private static final String XPATH_SUBJECT_DATA = "./SubjectData";
    /**
     * XPath Query: Study Event Data -- event related data, child of SubjectData
     */
    private static final String XPATH_STUDYEVENTDATA = "./StudyEventData";
    /**
     * XPath Query: All attributes that do not have their value set to '<VALUE>'
     */
    private static final String XPATH_SUBJECT_ATTRS = "./@*[.!='<VALUE>']";
    /**
     * Controls whether a preliminary consistency check must be preformed prior
     * to an insert or update transaction of ODM data. Be aware that this
     * entails that the complete study model is pre-loaded; in case of large
     * studies (e.g. the PREHDICT n=175509) this can overload the communication
     * chain (MirthCOnnect, OCSWM, OpenClinica).
     */
    private static final String ATTR_PRELIMANARY_CONSISTENCY_CHECK =
            "Mirth:PreliminaryConsistencyCheck";
    /**
     * study index. Study objects, keyed by study name + site name
     */
    protected HashMap<String, Study> studies; // // <unique id (study name + sitename), study object>
    /**
     * OC web services connector
     */
    private OCWebServices connector;

    /**
     * Constructor.
     *
     * @param ocConnector OpenClinica web services
     * @throws ODMException
     */
    public ClinicalODMResolver(OCWebServices ocConnector) throws ODMException {
        super();
        this.connector = ocConnector;
        studies = new HashMap<String, Study>();
    }

    /**
     * Create a ClinicalODMResolver, initializing its content from a Document.
     *
     * @param odm ODM XML document
     * @param ocConnector OpenClinica web services
     * @param clean Clean the ODM used for init or not
     * @throws ODMException
     * @throws OCConnectorException
     * @throws DatatypeConfigurationException
     */
    public ClinicalODMResolver(Document odm, OCWebServices ocConnector, boolean clean) throws ODMException,
            OCConnectorException, DatatypeConfigurationException {
        super(odm, clean);
        this.connector = ocConnector;
        studies = new HashMap<String, Study>();
    }

    /**
     * Create a ClinicalODMResolver, initializing its content from a String.
     *
     * @param odm ODM XML as String
     * @param ocConnector OpenClinica web services
     * @param clean Clean the ODM used for init or not
     * @throws ODMException
     * @throws OCConnectorException
     * @throws DatatypeConfigurationException
     */
    public ClinicalODMResolver(String odm, OCWebServices ocConnector, boolean clean) throws ODMException,
            OCConnectorException, DatatypeConfigurationException {
        super(odm, clean);
        studies = new HashMap<String, Study>();
    }

    /**
     * Create a ClinicalODMResolver, initializing its content from a Document,
     * with cleaning set to DEFAULT_CLEANING.
     *
     * @param odm ODM XML document
     * @param ocConnector OpenClinica web services
     * @throws ODMException
     * @throws OCConnectorException
     * @throws DatatypeConfigurationException
     */
    public ClinicalODMResolver(Document odm, OCWebServices ocConnector) throws ODMException, OCConnectorException,
            DatatypeConfigurationException {
        this(odm, ocConnector, DEFAULT_CLEANING);
        studies = new HashMap<String, Study>();
    }

    /**
     * removes all events in a ClinicalData-node which have the attribute Mirth:
     *
     * @param aClinicalDataNode
     * @return
     */
    public void removeEventsOnlyToSchedule(Document doc) throws ODMException {
        NodeList eventNodeList = xPath(doc, "//StudyEventData");
        for (int i = 0; i < eventNodeList.getLength(); i++) {
            Node eventNode = eventNodeList.item(i);
            if (scheduleOnly(eventNode)) {
                Node parent = eventNode.getParentNode();
                parent.removeChild(eventNode);
            }
        }
    }

    /**
     * Returns
     * <code>true</code> if there are child nodes
     * <code>EventData</code> in the parent node
     *
     * @param aClinicalDataNode
     * @return
     */
    public boolean hasEventToUpload(Node aClinicalDataNode) throws ODMException {
        NodeList eventNodeList = xPath(aClinicalDataNode, "//StudyEventData");
        return eventNodeList.getLength() > 0;
    }

    /**
     * Create a ClinicalODMResolver, initializing its content from a String,
     * with cleaning set to DEFAULT_CLEANING.
     *
     * @param odm ODM XML as String
     * @param ocConnector OpenClinica web services
     * @throws ODMException
     * @throws OCConnectorException
     * @throws DatatypeConfigurationException
     */
    public ClinicalODMResolver(String odm, OCWebServices ocConnector) throws ODMException, OCConnectorException,
            DatatypeConfigurationException {
        this(odm, ocConnector, DEFAULT_CLEANING);
        studies = new HashMap<String, Study>();
    }

    /**
     * Return boolean attribute ATTR_MIRTH_TRANSLATEOID as bool for a certain
     * node.
     *
     * @param node the nodes that supposedly has the ATTR_MIRTH_TRANSLATEOID
     * attribute
     * @return the boolean value as bool
     */
    private boolean translateOrNot(Node node) {
        return checkBooleanAttribute(node, ATTR_MIRTH_TRANSLATEOID);
    }

    /**
     * Return boolean attribute ATTR_MIRTH_CREATE as bool for a certain node.
     *
     * @param node the nodes that supposedly has the ATTR_MIRTH_CREATE attribute
     * @return the boolean value as bool
     */
    private boolean hasToBeCreated(Node node) {
        return checkBooleanAttribute(node, ATTR_MIRTH_CREATE);
    }

    /**
     * Return boolean attribute
     * <code>ATTR_MIRTH_SCHEDULE_ONLY</code> as bool for a certain node.
     *
     * @param node the nodes that supposedly has the ATTR_MIRTH_SCHEDULE_ONLY
     * attribute
     * @return the boolean value as bool
     */
    private boolean scheduleOnly(Node node) {
        return checkBooleanAttribute(node, ATTR_MIRTH_SCHEDULE_ONLY);
    }

    /**
     * Return boolean attribute value of
     * {@link #ATTR_PRELIMANARY_CONSISTENCY_CHECK}
     *
     * @param node the nodes that optionally has the
     * {@link #ATTR_PRELIMANARY_CONSISTENCY_CHECK} attribute
     * @return the attribute value
     */
    private boolean performPreliminaryConsistencyCheck(Node node) throws ODMException {
        return checkBooleanAttribute(node, ATTR_PRELIMANARY_CONSISTENCY_CHECK);
    }

    /**
     * Create StudySubject from SubjectData ODM element.
     *
     * @param study study the subject belongs to
     * @param subjectDataNode the node to be translated
     * @return a StudySubject object based on the SubjectData element supplied
     * @throws ODMException
     * @throws DatatypeConfigurationException
     */
    private StudySubject createStudySubject(Study study, Node subjectDataNode) throws ODMException {
        NodeList attributes = xPath(subjectDataNode, XPATH_SUBJECT_ATTRS);
        StudySubject studySubject = new StudySubject(study);
        String subjectHandle = "";
        for (int i = 0; i < attributes.getLength(); ++i) {
            Node node = attributes.item(i);
            String nodeName = node.getNodeName();
            String nodeValue = node.getNodeValue();
            if (ATTR_OC_DATEOFBIRTH.equals(nodeName)) {
                studySubject.setDateOfBirth(nodeValue);
            } else if (ATTR_OC_DATEOFREGISTRATION.equals(nodeName)) {
                studySubject.setDateOfRegistration(nodeValue);
            } else if (ATTR_OC_SEX.equals(nodeName)) {
                studySubject.setSex(nodeValue);
            } else if (ATTR_OC_SUBJECTKEY.equals(nodeName)) {
                subjectHandle = nodeValue;
            } else if (ATTR_OC_PERSONID.equals(nodeName)) {
                studySubject.setPersonID(nodeValue);
            } else if (ATTR_MIRTH_SITE_IDENDIFIER.equals(nodeName)) {
                studySubject.setSiteOID(nodeValue);
            }
            if (translateOrNot(subjectDataNode)) {
                studySubject.setStudySubjectLabel(subjectHandle);
            } else {
                studySubject.setStudySubjectOID(subjectHandle);
            }
        }
        return studySubject;
    }

    /**
     * Resolves a certain study with an optional center. See overloaded
     * resolveMe(). This method makes a OpenClinica web service call.
     *
     * @return a collection of studies
     * @throws ODMException
     * @throws OCConnectorException
     * @throws DatatypeConfigurationException
     */
    private Collection<Study> resolvStudy() throws ODMException, OCConnectorException {
        logger.debug("Resolving study");
        ListAllResponse allStudies = connector.listAllStudies(); // fetch available studies
        logger.debug("Resolved study; found " + allStudies.getStudies().getStudy().size());
        return resolveMe(allStudies);
    }

    /**
     * This is where all the logic sits for resolving ODM data. ODM data is
     * processed here and unresolved attributes are resolved, subjects are
     * created, events are scheduled... This method operated on a pre-fetched
     * ListAllResponse object.
     *
     * @param studies internal study data (our cache)
     * @param allStudies all studies from listAll OpenClinica method.
     * @return A collection of studies.
     * @throws ODMException
     * @throws OCConnectorException
     * @throws DatatypeConfigurationException
     */
    private Collection<Study> resolveMe(ListAllResponse allStudies)
            throws ODMException, OCConnectorException {

        this.clean();
        // TODO: Change the order of things in here slightly
        // so as to make this a bit more of a "transaction". A subject
        // should not be created if we know beforehand that the event
        // data is rubbish.

        // for each ClinicalData i
        NodeList clinicalDatas = xPath(XPATH_CLINICAL_DATA);
        logger.info("Processing clinicalDatas " + clinicalDatas.getLength());
        for (int i = 0; i < clinicalDatas.getLength(); ++i) {
            Node clinicalData = clinicalDatas.item(i); // ---- ClinicalData i ----
            Attr studyOID = getAttribute(clinicalData, ATTR_STUDYOID);
            Study study = connector.findStudy(allStudies, studyOID.getNodeValue(), !translateOrNot(clinicalData));
            studyOID.setNodeValue(study.getStudyOID()); // update OID field (it may have been translated)
            String studyHashKey = "Study: " + study.getStudyName() + ", Site: " + study.getSiteName();
            if (studies.containsKey(studyHashKey)) {
                study = studies.get(studyHashKey);
            } else {
                studies.put(studyHashKey, study);
                logger.debug("resolveMe() calling connector.populateStudy()");
                connector.populateStudy(study); // fetch study from OC
            }
            logger.debug("studies: " + studies.keySet());
            // for each SubjectData j
            NodeList subjectDatas = xPath(clinicalData, XPATH_SUBJECT_DATA);
            logger.debug("Found " + subjectDatas.getLength() + " subjects");
            for (int j = 0; j < subjectDatas.getLength(); ++j) {
                Node subjectData = subjectDatas.item(j); // ---- SubjectData j ----
                StudySubject subject = createStudySubject(study, subjectData);
                for (StudySubject s : study.getStudySubjects()) { // make sure we reuse existing subjects...
                    if (s.getStudySubjectLabel().equals(subject.getStudySubjectLabel())) {
                        subject = s;
                        logger.debug("Found subject " + s);
                        break;
                    }
                }
                handleSubjectDataNode(subjectData, subject);
                try {
                    connector.getSubjectOID(subject); // if this works it must be in the study (and in our model)
                } catch (OCConnectorException e) {
                    if (hasToBeCreated(subjectData)) {
                        logger.info("Creating study subject...");
                        connector.createStudySubject(subject);
                        connector.getSubjectOID(subject);
                        study.getStudySubjects().add(subject); // update model
                    } else {
                        logger.info("Failt to updateOID of subject " + subject);
                        throw e;
                    }
                }
                getAttribute(subjectData, "SubjectKey").setNodeValue(subject.getStudySubjectOID());

                // for each EventData k
                NodeList eventDatas = xPath(subjectData, XPATH_STUDYEVENTDATA);
                for (int k = 0; k < eventDatas.getLength(); ++k) {
                    String eventOID;
                    Node eventData = eventDatas.item(k); // ---- EventData k ----
                    eventOID = getAttribute(eventData, ATTR_STUDY_EVENT_OID).getNodeValue();
                    boolean hasEvent = false;
                    for (ScheduledEvent scheduledEvent : subject.getScheduledEvents()) {
                        if (scheduledEvent.getEventOID().equals(eventOID)) {
                            hasEvent = true;
                            break;
                        }
                    }
                    if (!hasEvent) { // no scheduled event

                        if (hasToBeCreated(eventData)) { // schedule one
                            logger.debug("Scheduling event with OID " + eventOID);
                            ScheduledEvent scheduledEvent = new ScheduledEvent(study.getEventDefinition(eventOID));
                            // scheduling happens here...
                            try {
                                scheduledEvent.setStartDate(getAttribute(eventData, ATTR_OC_START_DATE).getNodeValue());
                            } catch (ODMException e1) {
                                // Ignore odm exception here as we can do
                                // without a start date
                                logger.info("Problem with event startdate" + e1.getMessage() + ". Ignoring");
                            }
                            logger.info("Scheduling event " + eventOID + " at " + scheduledEvent.getStartDate());
                            connector.scheduleEvent(subject, scheduledEvent);
                            subject.getScheduledEvents().add(scheduledEvent); // update model
                        } else { // event not found (not scheduled)
                            throw new ODMException("Event with OID '" + eventOID + "' not found!");
                        }
                    }
                }
            }
        }
        removeAttributes(this.odm, "//@Mirth:*");
        removeAttributes(this.odm, "//@OpenClinica:*[.='<VALUE>']");
        return studies.values();
    }

    /**
     * Hook for additional node processing at subject data level
     *
     * @param subjectData SubjectData node
     * @param subject study subject
     * @throws ODMException
     */
    protected void handleSubjectDataNode(Node subjectData, StudySubject subject) throws ODMException {
    }

    /**
     * Hook for additional node processing at EventData level
     *
     * @param eventData event data node
     * @param event the event
     * @throws ODMException
     */
    protected void handleEventDataNode(Node eventData, ScheduledEvent event) throws ODMException {
    }

    /**
     * Resolve clinical OMD ("this")
     *
     * @return ref to clean "this" as a shortcut
     * @throws OCConnectorException
     * @throws ODMException
     * @throws DatatypeConfigurationException
     */
    public int resolveOdmDocument() throws OCConnectorException, ODMException {

        Node rootNode = xPath("/ODM", true).item(0);
        if (performPreliminaryConsistencyCheck(rootNode)) {
            resolvStudy();
            return 1;
        } else {
            resolveStudySubectsID();
            return 2;
        }
    }

    public void resolveStudySubectsID() throws ODMException, OCConnectorException {
        this.clean();

        NodeList clinicalDatas = xPath(XPATH_CLINICAL_DATA);
        logger.info("Processing clinicalDatas " + clinicalDatas.getLength());
        ListAllResponse allStudies = connector.listAllStudies(); // fetch available studies
        for (int i = 0; i < clinicalDatas.getLength(); ++i) {
            Node clinicalData = clinicalDatas.item(i); // ---- ClinicalData i ----
            Attr studyOID = getAttribute(clinicalData, ATTR_STUDYOID);
            Study study = connector.findStudy(allStudies, studyOID.getNodeValue(), !translateOrNot(clinicalData));
            study.setEvents(connector.fetchEventDefinitions(study)); // get events
            studyOID.setNodeValue(study.getStudyOID()); // update OID field (it may have been translated)
            String studyHashKey = "Study: " + study.getStudyName() + ", Site: " + study.getSiteName();
            if (studies.containsKey(studyHashKey)) {
                study = studies.get(studyHashKey);
            } else {
                studies.put(studyHashKey, study);
            }
            NodeList subjectDatas = xPath(clinicalData, XPATH_SUBJECT_DATA);
            logger.debug("Found " + subjectDatas.getLength() + " subjects");
            for (int j = 0; j < subjectDatas.getLength(); ++j) {
                Node subjectData = subjectDatas.item(j); // ---- SubjectData j ----
                StudySubject subject = createStudySubject(study, subjectData);

                if (hasToBeCreated(subjectData)) {
                    logger.info("Creating study subject...");
                    connector.createStudySubject(subject);
                    study.getStudySubjects().add(subject); // update model
                }

                IsStudySubjectResponse isStudySubjectResponse = connector.isStudySubject(subject);
                String subjectOID = isStudySubjectResponse.getStudySubjectOID();
                getAttribute(subjectData, "SubjectKey").setNodeValue(subjectOID);
                NodeList eventDatas = xPath(subjectData, XPATH_STUDYEVENTDATA);
                for (int k = 0; k < eventDatas.getLength(); ++k) {
                    String eventOID;
                    Node eventData = eventDatas.item(k); // ---- EventData k ----
                    eventOID = getAttribute(eventData, ATTR_STUDY_EVENT_OID).getNodeValue();
                    boolean hasEvent = false;
                    for (ScheduledEvent scheduledEvent : subject.getScheduledEvents()) {
                        if (scheduledEvent.getEventOID().equals(eventOID)) {
                            hasEvent = true;
                            break;
                        }
                    }
                    if (!hasEvent) { // no scheduled event

                        if (hasToBeCreated(eventData)) { // schedule one
                            logger.debug("Scheduling event with OID " + eventOID);
                            ScheduledEvent scheduledEvent = new ScheduledEvent(study.getEventDefinition(eventOID));
                            // scheduling happens here...
                            try {
                                scheduledEvent.setStartDate(getAttribute(eventData, ATTR_OC_START_DATE).getNodeValue());
                            } catch (ODMException e1) {
                                // Ignore odm exception here as we can do
                                // without a start date
                                logger.info("Problem with event startdate" + e1.getMessage() + ". Ignoring");
                            }
                            logger.info("Scheduling event " + eventOID + " at " + scheduledEvent.getStartDate());
                            connector.scheduleEvent(subject, scheduledEvent);
                            subject.getScheduledEvents().add(scheduledEvent); // update model
                        } else { // event not found (not scheduled)
                            throw new ODMException("Event with OID '" + eventOID + "' not found!");
                        }
                    }
                }
            }
        }
    }

    /**
     * // TODO: The cleaning part needs to be cleaned up and perhaps remodeled.
     * Clean "this" but more than with clean(), hence the name...
     *
     * @return clean version of "this" as a shortcut.
     * @throws ODMException
     */
    public ClinicalODM extraClean() throws ODMException {
        logger.debug("extraClean() (before): " + this);
        removeAttributes(this.odm, "//@Mirth:*");
        removeAttributes(this.odm, "//@OpenClinica:*[.='<VALUE>']");
        removeAttributes(this.odm, "//@StudyEventRepeatKey[.='<VALUE>']");
        removeAttributes(this.odm, "//@ItemGroupRepeatKey[.='<VALUE>']");
        logger.debug("extraClean() (after): " + this);
        return this;
    }

    /**
     * Clear all study data
     */
    public void clearCache() {
        studies = new HashMap<String, Study>(); // <unique id (study name + sitename), study object>
    }

    /**
     * Return web service connector
     *
     * @return web service connector
     */
    public OCWebServices getConnector() {
        return connector;
    }

    /**
     * Set web service connector
     *
     * @param connector web service connector
     */
    public void setConnector(OCWebServices connector) {
        this.connector = connector;
    }
}
