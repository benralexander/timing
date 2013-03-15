package timtest

class ExecuteCmdsController {
    TestingService testingService

    def test1(){
        LinkedHashMap<String, Object> linkedHashMap = testingService.findAllAssays()
        List assayList =  linkedHashMap."allAssays"
        render(view: 'test1', model: ["elapsedTime" : linkedHashMap."elapsedTime", "allAssays" : assayList ])
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

    def index() {
        render(view: 'index')
    }
}
