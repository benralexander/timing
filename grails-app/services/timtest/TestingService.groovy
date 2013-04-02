package timtest

import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import groovy.json.JsonSlurper
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import org.perf4j.LoggingStopWatch
import org.perf4j.StopWatch

import static groovyx.net.http.ContentType.URLENC

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
            assayIDs << retriever (element, /\w+/, 1, "/assays/xxxx")
       //     assayIDs << element.split("/")[2]
        returnValue."allAssays"  =  assayIDs
        return returnValue
    }

    LinkedHashMap<String, Object>  findAllProjects() {
        LinkedHashMap<String, Object>  returnValue = [:]
        RetFromCall retFromCall = timeRestCall("http://bard.nih.gov/api/v15/projects")
        returnValue."elapsedTime" =  retFromCall.elapsedTime
        List <String> assayIDs  = []
        for (String element in retFromCall.collection )
            assayIDs << retriever (element, /\w+/, 1, "/projects/xxxx")
        //    assayIDs << element.split("/")[2]
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
        println "api time ${returnValue."elapsedTime"}"
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
            String tempVersion = unparsedExpt.split('"')[0]
            experimentIDs << tempVersion.trim()
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


    LinkedHashMap<String, Object> convertCompoundsToSids(List<String> compoundList)  {
        LinkedHashMap<String, Object>  returnValue = [:]
        List<String>  sidList = []
        int totalElapsedTime  = 0
        for ( String individualCompound in compoundList)  {
            Boolean keepGoing = true
            String coreQuery = "http://bard.nih.gov/api/v15"
            String currentQuery =  "${coreQuery}/substances/cid/${individualCompound}"
            while (keepGoing) {
                RetFromCall retFromCall = timeResposeEntityCall(currentQuery)
                String elapsedTimeForThisCall =  retFromCall.elapsedTime
                int elapsedTimeAsInt =  0
                try{
                    elapsedTimeAsInt = Integer.parseInt(elapsedTimeForThisCall)
                } catch(Exception e)  {
                    assert false,"We should never have failed string conversion here"
                }
                totalElapsedTime +=  elapsedTimeAsInt
                List<String> eachRetLine =  retFromCall.body.split(/,/)
                for (String oneLine in eachRetLine) {
                    String sid =  oneLine.split("/")[2].split("\"")[0]
                    if (!sidList.contains(sid))
                        sidList <<  sid.trim()
                }
                if (retFromCall.link != null)
                    currentQuery =  "${coreQuery}${retFromCall.link}"
                else
                    keepGoing = false
            }
        }
        returnValue."elapsedTime" =  totalElapsedTime
        returnValue."sids"  =  sidList
        return returnValue
    }



    LinkedHashMap<String, Object> getMixedExptData(List<String> eidList,List<String> sidList)  {
        LinkedHashMap<String, Object>  returnValue = [:]
        int totalElapsedTime  = 0
            Boolean keepGoing = true
            String coreQuery = "http://bard.nih.gov/api/v15"
            String currentQuery =  "${coreQuery}/exptdata"
            while (keepGoing) {
                RetFromCall retFromCall = postRestCallRESTClient(currentQuery,eidList,sidList)
                String elapsedTimeForThisCall =  retFromCall.elapsedTime
                int elapsedTimeAsInt =  0
                try{
                    elapsedTimeAsInt = Integer.parseInt(elapsedTimeForThisCall)
                } catch(Exception e)  {
                    assert false,"We should never have failed string conversion here"
                }
                totalElapsedTime +=  elapsedTimeAsInt
                if (retFromCall.link != null)
                    currentQuery =  "${coreQuery}${retFromCall.link}"
                else
                    keepGoing = false
            }
        returnValue."elapsedTime" =  totalElapsedTime
        return returnValue

    }






    LinkedHashMap<String, Object> convertProjectsToExperiments (List<String> projects)  {
        LinkedHashMap<String, Object>  returnValue = [:]
        List<String>  allExperiments = []
        for (String proj in projects){
            LinkedHashMap<String, Object> retrievedExperimentMap = findAllExperimentsPerProject( proj )
            List experimentList =  retrievedExperimentMap."allExperiments"
            returnValue.elapsedTime = retrievedExperimentMap."elapsedTime".toString()
            for (String experiment in experimentList) {
                if (!allExperiments.contains(experiment)){
                    allExperiments <<  experiment
                }
            }
        }
        returnValue.eids =  allExperiments
        returnValue
    }



    LinkedHashMap<String, Object>  findAllActivities(List experimentList ) {
        LinkedHashMap<String, Object>  returnValue = [:]
        int numberOfCompounds = 0
        Long accumulatingTime =0L
        int loops = 0
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
                print "(${++loops}) time exptdata = ${retFromCall.elapsedTime}"
            }
        }
        returnValue."elapsedTime" =  accumulatingTime
        returnValue."numberOfCompoundActivities"  =  numberOfCompounds
        return returnValue
    }



    RetFromCall postRestCall(String urlSpecification,List<String> eidList,List<String> sidList) {
        RESTClient http = new RESTClient(urlSpecification)
        HttpResponseException httpResponseException
        Closure c = {
                sids = sidList.join(",")
                eids = eidList.join(",")
        }
        def f = restBuilder.post(urlSpecification,c)
        new RetFromCall()
    }












    void examineTarget(String accessionNumber,JsonSlurper jsonSlurper,FileWriter fileWriter,List<String>  recordedTargets  ) {
        LinkedHashMap<String, Object> returnValue = [:]

        String coreQuery = "http://bard.nih.gov/api/v15"
        String currentQuery = "${coreQuery}/targets/accession/${accessionNumber}"
        RetFromCall retFromCall = timeResposeEntityCall(currentQuery)
        String elapsedTimeForThisCall = retFromCall.elapsedTime
        int elapsedTimeAsInt = 0
        try {
            elapsedTimeAsInt = Integer.parseInt(elapsedTimeForThisCall)
        } catch (Exception e) {
            assert false, "We should never have failed string conversion here"
        }
        def f = retFromCall.body

        def g = jsonSlurper.parseText(f)
        List<RingNode>  ringNodeList = []
        // get the notes out of the Json and put them into a sorted data structure
        if (g.classes.size() > 0) {
            int numberOfClasses = g.classes.size()
            for (def targetClass in g.classes) {
                ringNodeList << new RingNode(targetClass."name",
                        targetClass."id",
                        targetClass."description",
                        targetClass."levelIdentifier",
                        targetClass."source")
            }

            ringNodeList.sort {RingNode ringNodeA, RingNode ringNodeB -> ringNodeA.numberOfLevels() <=> ringNodeB.numberOfLevels() }
            // link up that list of data structures into a tree
            LinkedHashMap<String, RingNode> ringNodeMgr = [:]
            ringNodeMgr["1."] = new RingNode("\\", "0", "root", "1", "none")
            for (RingNode ringNode in ringNodeList) {
                RingNode currentRingNode = ringNodeMgr["1."]
                List<String> listOfGroups = ringNode.levelIdentifier.split(/\./)
                String buildingString = ""
                for (String oneGroup in listOfGroups) {
                    buildingString += "${oneGroup}."
                    if (ringNodeMgr.containsKey(buildingString)) {
                        currentRingNode = ringNodeMgr[buildingString]
                    } else {
                        ringNodeMgr[buildingString] = ringNode
                        currentRingNode.children << ringNode
                        break
                    }
                }
            }
            // Everything node (except the root) now gets written out, and tree position is explicitly represented
            ringNodeMgr.each { String key, RingNode ringNode ->
                if (key != '1.')  {
                    fileWriter << ringNode.ID << '\t' << ringNode.name << '\t' << ringNode.description << '\t' <<
                            ringNode.levelIdentifier << '\t' << ringNode.source << '\t' << accessionNumber << '\t' <<
                            ringNode.writeHierarchyPath(ringNodeMgr) << '\n'

                }
            }
            fileWriter.flush()
        }
        returnValue."elapsedTime" = elapsedTimeAsInt
//        returnValue."sids" = sidList
        return //returnValue
    }



//    //for (def targetClass in g.classes){
//    def  targetClass =  g.classes[numberOfClasses-1]
//    if (!recordedTargets.contains(targetClass."id")) {
//        recordedTargets << targetClass."id"
//        fileWriter <<  targetClass."id" << '\t' <<  targetClass."name" << '\t' << targetClass."description" << '\t' <<
//                targetClass."levelIdentifier" << '\t' << targetClass."source" << '\t' << accessionNumber  << '\n'
//        if ((recordedTargets.size()%50) == 0)
//            println "We are up to ${recordedTargets.size()} unique IDs"
//        fileWriter.flush()
//    }
//    //}









    LinkedHashMap<String, Object> stepThroughAllTargets() {
        LinkedHashMap<String, Object> returnValue = [:]
        List<String>  recordedTargets = []
        int totalElapsedTime = 0
        Boolean keepGoing = true
        File file=new File("target.txt")
        FileWriter fileWriter = new FileWriter(file)
        JsonSlurper jsonSlurper =  new JsonSlurper()
        String coreQuery = "http://bard.nih.gov/api/v15"
        String currentQuery = "${coreQuery}/targets"
        while (keepGoing) {
            RetFromCall retFromCall = timeRestCall(currentQuery)
            for (String cmpdstr in retFromCall.collection) {
                String cmpd = retriever (cmpdstr, /\w+/, 2, "/targets/accession/xxxxxx")
                examineTarget (cmpd, jsonSlurper,fileWriter,recordedTargets)
            }
            if (retFromCall.link != null)
                currentQuery = "${coreQuery}${retFromCall.link}"
            else
                keepGoing = false
            int elapsedTimeAsInt = 0
            try {
                elapsedTimeAsInt = Integer.parseInt(retFromCall.elapsedTime.toString())
            } catch (Exception e) {
                assert false, "We should not have failed string conversion.  Expected number, received ${retFromCall.elapsedTime}"
            }
            totalElapsedTime += elapsedTimeAsInt
        }

        returnValue."elapsedTime" = totalElapsedTime
        return returnValue
    }




    String retriever (String incomingString, String regularExpression, int indexOfDesiredString, String textOfErrorMessage) {
        java.util.regex.Matcher matcher = incomingString =~ regularExpression
        assert matcher.getCount() > indexOfDesiredString, "ERROR: Expected string of form ${textOfErrorMessage}, but received ${incomingString}"
        return matcher[indexOfDesiredString]
    }



    RetFromCall postRestCallRESTClient(String urlSpecification,List<String> eidList,List<String> sidList) {
        RetFromCall retFromCall = new RetFromCall()
        RESTClient http = new RESTClient(urlSpecification)
        stopWatch.start()
        try{
            HttpResponseException httpResponseException
            def postBody = [sids:sidList.join(","),eids:eidList.join(",") ]

            HttpResponseDecorator serverResponse =
                (HttpResponseDecorator) http.post(
                        path: urlSpecification,
                        body: postBody,
                        requestContentType: URLENC
                )
        }  catch (Exception e){
            e.printStackTrace()
        }
        stopWatch.stop("timereq", urlSpecification);
        retFromCall.elapsedTime = stopWatch.elapsedTime
        retFromCall
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
        retFromCall
    }




}


class RetFromCall{
 Long elapsedTime   = null
    List collection = null
    String body = null
    String link = null
}