package timtest

class ExecuteCmdsController {
    TestingService testingService

    def test1(){
        LinkedHashMap<String, Object> linkedHashMap = testingService.findAllAssays()
        List assayList =  linkedHashMap."allAssays"
        render(view: 'test1', model: ["elapsedTime" : linkedHashMap."elapsedTime", "allAssays" : assayList ])
    }
    def assaylist(){
        LinkedHashMap<String, Object> linkedHashMap = testingService.findAllAssays()
        List assayList =  linkedHashMap."allAssays"
        render(view: 'assaylist', model: ["elapsedTime" : linkedHashMap."elapsedTime", "allAssays" : assayList ])
    }
    def projectlist(){
        LinkedHashMap<String, Object> linkedHashMap = testingService.findAllProjects()
        List assayList =  linkedHashMap."allAssays"
        render(view: 'projectlist', model: ["elapsedTime" : linkedHashMap."elapsedTime", "allAssays" : assayList ])
    }

    def test1p(){
        LinkedHashMap<String, Object> linkedHashMap = testingService.findAllProjects()
        List assayList =  linkedHashMap."allAssays"
        render(view: 'test1p', model: ["elapsedTime" : linkedHashMap."elapsedTime", "allAssays" : assayList ])
    }

    def test2(){
        String assayId = params.assayid
        LinkedHashMap<String, Object> retrievedExperimentMap = testingService.findAllExperimentsPerAssay( assayId)
        List experimentList =  retrievedExperimentMap."allExperiments"
        LinkedHashMap<String, Object> findAllActivities = testingService.findAllActivities( experimentList )
        Long totalElapsedTime = findAllActivities."elapsedTime" + retrievedExperimentMap."elapsedTime"
        render(view: 'test2', model: ["elapsedTime" : totalElapsedTime, "allExperiments":retrievedExperimentMap."allExperiments","numberOfCompoundActivities" : findAllActivities."numberOfCompoundActivities" ])
    }
    def test2p(){
        String projectId = params.projectid
        LinkedHashMap<String, Object> retrievedExperimentMap = testingService.findAllExperimentsPerProject( projectId)
        List experimentList =  retrievedExperimentMap."allExperiments"
        LinkedHashMap<String, Object> findAllActivities = testingService.findAllActivities( experimentList )
        Long totalElapsedTime = findAllActivities."elapsedTime" + retrievedExperimentMap."elapsedTime"
        render(view: 'test2', model: ["elapsedTime" : totalElapsedTime, "allExperiments":retrievedExperimentMap."allExperiments","numberOfCompoundActivities" : findAllActivities."numberOfCompoundActivities" ])
    }

    def cmpdsperproj(){
        String projectId = params.projectid?.trim()
        LinkedHashMap<String,List<String>> cmpdsToExpts = [:]
        List<String> diffProjs =  projectId.split(/,/)
        int totalNumberOfExperiments = 0
        for (String proj in diffProjs){
            LinkedHashMap<String, Object> retrievedExperimentMap = testingService.findAllExperimentsPerProject( proj )
            List experimentList =  retrievedExperimentMap."allExperiments"
            def t = testingService.returnAllCompounds( cmpdsToExpts, experimentList )
            print "numberOfExperiments${t."numberOfExperiments"} in project ${proj}"
            totalNumberOfExperiments  +=  t."numberOfExperiments"
        }
        // find widely used compounds
        int numberOfCompoundsWeWant = 50
        int numberOfTimesSeen =  totalNumberOfExperiments
        List<String> compoundsWeWant =[]
        while ((cmpdsToExpts.find{it.value.size()==numberOfTimesSeen}==null)  && (numberOfTimesSeen>0)){
            numberOfTimesSeen--
        }
        int numberOfCompoundsWeHave = 0
        while ((numberOfCompoundsWeHave < numberOfCompoundsWeWant) && (numberOfTimesSeen > 0)){
            if (cmpdsToExpts.findAll{it.value.size()==numberOfTimesSeen} != null){
                for ( String oneCompound in cmpdsToExpts.findAll{it.value.size()==numberOfTimesSeen}*.keys) {
                    if   (compoundsWeWant.size() < numberOfCompoundsWeWant)
                        compoundsWeWant <<  oneCompound
                }
                numberOfCompoundsWeHave =  compoundsWeWant.size()
                numberOfTimesSeen--
            }

        }
        println("Found ${cmpdsToExpts.size()} original cmpds. Selected ${numberOfCompoundsWeHave}")
    }

    def activitiesacrossprojs(){
        String projectIds = params.projectids?.trim()
        String compoundIds = params.compoundids?.trim()
        List<String> rawProjs =  projectIds.split(/,/)
        List<String> rawCompounds =  compoundIds.split(/ /)
        List<String> projectList = []
        for (String aProject in rawProjs) {
            projectList << aProject.trim()
        }
        List<String> compoundList = []
        for (String aCompound in rawCompounds) {
            compoundList << aCompound.trim()
        }
        int totalNumberOfExperiments = 0
        LinkedHashMap<String, Object>  allExperiments = testingService.convertProjectsToExperiments(projectList)
        println "get projs = ${allExperiments.elapsedTime}"
        LinkedHashMap<String, Object> allSids = testingService.convertCompoundsToSids(compoundList)
        println "get cmpds = ${allSids.elapsedTime}"
        LinkedHashMap<String, Object> finalResults =testingService.getMixedExptData(allExperiments.eids,allSids.sids)
        println "get cmpds = ${finalResults.elapsedTime}"

    }

    def pantherproj(){

    }


    def index() {
        render(view: 'index')
    }
}
