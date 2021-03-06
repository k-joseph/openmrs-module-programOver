/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.programOver.web.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.api.context.Context;
import org.openmrs.module.programOver.advice.UsageStatsUtils;
import org.openmrs.module.programOver.db.hibernate.ProgramOverviewDAOimpl;
import org.openmrs.module.regimenhistory.Regimen;
import org.openmrs.module.regimenhistory.RegimenComponent;
import org.openmrs.module.regimenhistory.RegimenUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.mvc.SimpleFormController;

/**
 *
 */
public class PatientDownloadController extends SimpleFormController {
	
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	private SessionFactory sessionFactory;
	
	/**
	 * @param sessionFactory the sessionFactory to set
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}
	
	/**
	 * @see org.springframework.web.servlet.mvc.AbstractController#handleRequestInternal(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String programIdKey = "";
		
		if (request.getParameter("checkType") != null) {
			
			programIdKey = request.getParameter("checkType");
			
			//		Date lastMidnight = UsageStatsUtils.getPreviousMidnight(null);
			//		Date twoWeekAgo = UsageStatsUtils.addDaysToDate(lastMidnight, -14);
			
			List<Object[]> listPatientHistory = (List<Object[]>) request.getSession().getAttribute(
			    "patientSize_" + (Integer.parseInt(request.getParameter("lineNumber")) - 1));
			
			
			doDownload(request, response, listPatientHistory, "patients List.csv", programIdKey);
		}
		return null;
	}
	
	/**
	 * Auto generated method comment
	 * 
	 * @param request
	 * @param response
	 * @param patients
	 * @param filename
	 * @param title
	 * @throws IOException
	 */
	private void doDownload(HttpServletRequest request, HttpServletResponse response, List<Object[]> listPatientHistory,
	                        String filename, String title) throws IOException {
		SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		ServletOutputStream outputStream = response.getOutputStream();
		List<Regimen> regimens = new ArrayList<Regimen>();
		Set<RegimenComponent> components = new HashSet<RegimenComponent>();
		ProgramOverviewDAOimpl programDaoImpl = new ProgramOverviewDAOimpl();
		
		List<Object> nameOfeventDate = new ArrayList<Object>();
		List<Object> returnVisitDates = new ArrayList<Object>();
		List<Object> consultationDates = new ArrayList<Object>();
		List<Object> drugTitles = new ArrayList<Object>();
		List<Object> startingARVDate = new ArrayList<Object>();
		List<Object> CD4CountDate = new ArrayList<Object>();
		
		response.setContentType("text/plain");
		response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
		outputStream.println("" + title);
		outputStream.println();
		
		Object eventDate = null;
		Object eventDate1 = null;
		Object regimenTitle = null;
		Object consultatioDatetitle = null;
		Object startingARV = null;
		Object CD4count = null;
		
		for (Object[] objects : listPatientHistory) {
			//the object defined below holds the title of added columns
			if (objects.length > 3 && objects.length <= 5) {
				nameOfeventDate.add(objects[2]);
				returnVisitDates.add(objects[4]);
				eventDate = nameOfeventDate.get(0);
				eventDate1 = returnVisitDates.get(0);
				//drugTitles.add(objects[6]);
				//regimenTitle = drugTitles.get(0);
				outputStream.println("Identifier , Given Name , Family Name , Age , Gender , " + eventDate
				        + ", " + eventDate1 + "");
				
			}
			if (objects.length == 3) {
				nameOfeventDate.add(objects[2]);
				eventDate = nameOfeventDate.get(0);
				outputStream.println("Identifier , Given Name , Family Name , Age , Gender , " + eventDate + "");
				
			}
			if (objects.length > 5){
				//System.out.println(">>>>>>>eligibility");
				nameOfeventDate.add(objects[2]);
				returnVisitDates.add(objects[4]);
				eventDate = nameOfeventDate.get(0);
				eventDate1 = returnVisitDates.get(0);
				//drugTitles.add(objects[6]);
				//regimenTitle = drugTitles.get(0);
				consultationDates.add(objects[6]);
				consultatioDatetitle = consultationDates.get(0);
				//startingARVDate.add(objects[8]);
				//CD4CountDate.add(objects[10]);
				
				
				outputStream.println("Identifier , Given Name , Family Name , Age , Gender , " + eventDate
				        + " , " + eventDate1 + "," + consultatioDatetitle  +  "");
			}
						//outputStream
					     //   .println("Identifier , Given Name , Family Name , Age , Gender , Birth Day , last encounter date , return visit day , regimen");
			
			outputStream.println();
			for (Object[] object : listPatientHistory) {
				if (object.length > 3 && object.length <= 5) {
					Patient patient = (Patient) object[0];
					
					Date encounterDate = (Date) object[1];
					Date returnVisitDay = (Date) object[3];
					
//					if (RegimenUtils.getRegimenHistory(Context.getPatientService().getPatient(patient.getPatientId()))
//					        .getRegimenList().size() != 0)
//						regimens = RegimenUtils.getRegimenHistory(
//						    Context.getPatientService().getPatient(patient.getPatientId())).getRegimenList();
//					
//					for (Regimen r : regimens) {
//						components = r.getComponents();
//					}
					if(patient.getGivenName()!=null){
					log.info(">>>>>>This is the given nameeeeeeeeeeeeeeeeeeeeeeeee:"+patient.getGivenName());
					outputStream.println(patient.getPatientId().toString() + " , " + patient.getGivenName().toString()
			        + " , " + patient.getFamilyName().toString() + " , " + patient.getAge().toString() + " , "
			        + patient.getGender().toString() + " , " 
			        + encounterDate + " , " + returnVisitDay);
					}
					else{
						log.info(">>>>>>This is nullllllllllllllllllllllll ");
						outputStream.println(patient.getPatientId().toString() + " , " + ""
					        + " , " + patient.getFamilyName().toString() + " , " + patient.getAge().toString() + " , "
					        + patient.getGender().toString() + " , " 
					        + encounterDate + " , " + returnVisitDay);
					}
					
					
					/*outputStream.println(patient.getPatientId().toString() + " , " + patient.getGivenName().toString() !=null ? patient.getGivenName().toString() : " "
					        + " , " + patient.getFamilyName().toString() + " , " + patient.getAge().toString() + " , "
					        + patient.getGender().toString() + " , " 
					        + encounterDate + " , " + returnVisitDay);*/
					
				}
				
				if (object.length == 3) {
					Patient patient = (Patient) object[0];
					Date encounterDate = (Date) object[1];
					outputStream.println(patient.getPatientId().toString() + " , " + patient.getGivenName().toString()
					        + " , " + patient.getFamilyName().toString() + " , " + patient.getAge().toString() + " , "
					        + patient.getGender().toString() + " , "
					        + encounterDate);
					
				}
				if (object.length > 5) {
					
				
					
					Patient patient = (Patient)object[0];					
					Date encounterDate = (Date) object[1];
					Date returnVisitDay = (Date) object[3];
					Date consultationDate = (Date) object[5];
					
					
					//Date whenARVStarted=(Date) object[7];
					//String stringCd4Count= (String) object[9];
					System.out.println();
				
					
					//System.out.println(" patient whose eligiblity is there");
					 
					outputStream.print(patient.getPatientId().toString() + " , " + patient.getGivenName().toString() + " , "
					        + patient.getFamilyName().toString() + " , " + patient.getAge().toString() + " , "
					        + patient.getGender().toString() + " , "
					        + encounterDate + " , " + returnVisitDay);
					
//					if (RegimenUtils.getRegimenHistory(Context.getPatientService().getPatient(patient.getPatientId()))
//					        .getRegimenList().size() != 0)
//						regimens = RegimenUtils.getRegimenHistory(
//						    Context.getPatientService().getPatient(patient.getPatientId())).getRegimenList();
					
					outputStream.print(" , " + consultationDate);
					
					outputStream.println();					
//					for (Regimen regimen : regimens){
//						components = regimen.getComponents();						
//						outputStream.println("," + "" + "," + "" + "," + "" + "," + "" + "," + "" + "," + "" + "," + ""
//						        + "," + "" + " , " + programDaoImpl.getRegimensAsString(components));
//					}
					
				}
				
				if (object.length == 1){
					
					Patient patient = (Patient) object[0];
					outputStream.println(patient.getPatientId().toString() + " , " + patient.getGivenName().toString()
					        + " , " + patient.getFamilyName().toString() + " , " + patient.getAge().toString() + " , "
					        + patient.getGender().toString());
					
				}
				
			}
			
			outputStream.flush();
			outputStream.close();
		}
		
	}
}
