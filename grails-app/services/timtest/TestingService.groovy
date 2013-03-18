package timtest

import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import groovy.json.JsonSlurper
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import org.perf4j.LoggingStopWatch
import org.perf4j.StopWatch

class TestingService {

    RestBuilder restBuilder = new  RestBuilder()
    StopWatch stopWatch = new LoggingStopWatch()
    JsonSlurper jsonSlurper = new JsonSlurper()


    String serviceMethod() {
        return "time required=${timeRestCall("http://bard.nih.gov/api/v15/assays")}"
    }

    LinkedHashMap<String, Object>  findAllAssays() {
        LinkedHashMap<String, Object>  returnValue = [:]
        RetFromCall retFromCall = timeRestCall("http://bard.nih.gov/api/v15/assays")
        returnValue."elapsedTime" =  retFromCall.elapsedTime
        List <String> assayIDs  = []
        for (String element in retFromCall.collection )
            assayIDs << element.split("/")[2]
        returnValue."allAssays"  =  assayIDs
        return returnValue
    }

    LinkedHashMap<String, Object>  findAllProjects() {
        LinkedHashMap<String, Object>  returnValue = [:]
        RetFromCall retFromCall = timeRestCall("http://bard.nih.gov/api/v15/projects")
        returnValue."elapsedTime" =  retFromCall.elapsedTime
        List <String> assayIDs  = []
        for (String element in retFromCall.collection )
            assayIDs << element.split("/")[2]
        returnValue."allAssays"  =  assayIDs
        return returnValue
    }

    LinkedHashMap<String, Object>  findAllExperimentsPerAssay(String assayId) {
        LinkedHashMap<String, Object>  returnValue = [:]
        RetFromCall retFromCall = timeResposeEntityCall("http://bard.nih.gov/api/v15/assays/${assayId}/experiments")
        returnValue."elapsedTime" =  retFromCall.elapsedTime
        List <String> experimentIDs  = []
        String unparsedExpt =  retFromCall.body.split("/")[2]
        experimentIDs << unparsedExpt.split('"')[0]
        returnValue."allExperiments"  =   experimentIDs
        return returnValue
    }

    LinkedHashMap<String, Object>  findAllExperimentsPerProject(String projectId) {
        LinkedHashMap<String, Object>  returnValue = [:]
        RetFromCall retFromCall = timeResposeEntityCall("http://bard.nih.gov/api/v15/projects/${projectId}/experiments")
        returnValue."elapsedTime" =  retFromCall.elapsedTime
        List <String> experimentIDs  = []
        List <String> groupIndividualExperiment = retFromCall.body.split(',')
        for ( String individualExperimentLine in groupIndividualExperiment) {
            String unparsedExpt =  individualExperimentLine.split("/")[2]
            experimentIDs << unparsedExpt.split('"')[0]
        }
        returnValue."allExperiments"  =   experimentIDs
        return returnValue
    }

    LinkedHashMap<String, Object>  findAllCompounds(String experimentID) {
        LinkedHashMap<String, Object>  returnValue = [:]
        int numberOfCompounds = 0
        Boolean keepGoing = true
        for ( String individualExperiment in experimentList)  {
            while (keepGoing) {
                RetFromCall retFromCall = timeRestCall("http://bard.nih.gov/api/v15/assays/${individualExperiment}/compounds")
                numberOfCompounds += 500
                returnValue."elapsedTime" +=  retFromCall.elapsedTime
            }
        }
        returnValue."elapsedTime" =  retFromCall.elapsedTime
        returnValue."numberOfCompoundActivities"  =  numberOfCompounds
        return returnValue
    }

    LinkedHashMap<String, Object>  returnAllCompounds(LinkedHashMap<String,List<String>> cmptsToExpts,List experimentList) {
        LinkedHashMap<String, Object>  returnValue = [:]
        int numberOfCompounds = 0
        Long accumulatingTime =0L
        int numberOfExperiments = 0
        for ( String individualExperiment in experimentList)  {
            numberOfExperiments++
            Boolean keepGoing = true
            String coreQuery = "http://bard.nih.gov/api/v15"
            String currentQuery =  "${coreQuery}/experiments/${individualExperiment}/compounds"
            while (keepGoing) {
                RetFromCall retFromCall = timeRestCall(currentQuery)
                for (String cmpdstr in retFromCall.collection) {
                    String cmpd =  cmpdstr.split("/")[2]
                    if (cmptsToExpts.containsKey(cmpd))
                        cmptsToExpts[cmpd]  << individualExperiment
                    else
                        cmptsToExpts.put(cmpd,[individualExperiment])
                }
                if (retFromCall.link != null)
                    currentQuery =  "${coreQuery}${retFromCall.link}"
                else
                    keepGoing = false
            }
        }
        returnValue."elapsedTime" =  accumulatingTime
        returnValue."numberOfExperiments"  =   numberOfExperiments
        return returnValue
    }



    LinkedHashMap<String, Object>  findAllActivities(List experimentList ) {
        LinkedHashMap<String, Object>  returnValue = [:]
        int numberOfCompounds = 0
        Long accumulatingTime =0L
        for ( String individualExperiment in experimentList)  {
            Boolean keepGoing = true
            String coreQuery = "http://bard.nih.gov/api/v15"
            String currentQuery =  "${coreQuery}/experiments/${individualExperiment}/exptdata?expand=true"
            while (keepGoing) {
                RetFromCall retFromCall = timeRestCall(currentQuery)
                numberOfCompounds += retFromCall.collection.size()
                accumulatingTime +=  retFromCall.elapsedTime
                if (retFromCall.link != null)
                    currentQuery =  "${coreQuery}${retFromCall.link}"
                else
                    keepGoing = false
            }
        }
        returnValue."elapsedTime" =  accumulatingTime
        returnValue."numberOfCompoundActivities"  =  numberOfCompounds
        return returnValue
    }


    RetFromCall timeRestCall(String urlSpecification) {
        RetFromCall retFromCall = new RetFromCall()
        stopWatch.start()
        RestResponse restResponse = restBuilder.get(urlSpecification)
        stopWatch.stop("timereq", urlSpecification);
        retFromCall.elapsedTime = stopWatch.elapsedTime
        JSONObject jsonObject = restResponse.json
        JSONArray jsonArray = jsonObject."collection"
        retFromCall.collection =  jsonArray*.toString()
        if (jsonObject.link)
            retFromCall.link =  jsonObject.link
        retFromCall
    }


    RetFromCall timeResposeEntityCall(String urlSpecification) {
        RetFromCall retFromCall = new RetFromCall()
        stopWatch.start()
        RestResponse restResponse = restBuilder.get(urlSpecification)
        stopWatch.stop("timereq", urlSpecification);
        retFromCall.elapsedTime = stopWatch.elapsedTime
        retFromCall.body =  restResponse.responseEntity.body
        //retFromCall.link ?: restResponse.link
        retFromCall
    }

}


class RetFromCall{
 Long elapsedTime   = null
    List collection = null
    String body = null
    String link = null
}