package com.hmtmcse.dtm.definition

import com.hmtmcse.gs.GsApiActionDefinition
import com.hmtmcse.gs.data.ApiHelper
import com.hmtmcse.gs.data.GsApiResponseData
import com.hmtmcse.gs.data.GsFilteredData
import com.hmtmcse.gs.data.GsParamsPairData
import com.hmtmcse.gs.model.CustomProcessor
import com.hmtmcse.gs.model.RequestPreProcessor
import com.hmtmcse.swagger.SwaggerHelper
import com.hmtmcse.swagger.definition.SwaggerConstant
import com.hmtmcse.dtm.StepService
import com.hmtmcse.dtm.Steps
import grails.web.servlet.mvc.GrailsParameterMap


class StepsDefinitionService {

    StepService stepService

    static GsApiActionDefinition detailsDefinition() {
        GsApiActionDefinition gsApiActionDefinition = new GsApiActionDefinition<Steps>(Steps)
        gsApiActionDefinition.includeAllNotRelationalThenExcludeFromResponse(DefinitionCommonService.commonSkipFields())
        return gsApiActionDefinition
    }

    GsApiActionDefinition details() {
        GsApiActionDefinition gsApiActionDefinition = detailsDefinition()
        gsApiActionDefinition.includeAllPropertyToResponse()
        gsApiActionDefinition.addToWhereFilterProperty("id").enableTypeCast()
        return gsApiActionDefinition
    }

    GsApiActionDefinition list() {
        GsApiActionDefinition gsApiActionDefinition = detailsDefinition()
        gsApiActionDefinition.includeAllPropertyToResponse()
        gsApiActionDefinition.addRelationalEntityResponse("complexity")
        gsApiActionDefinition.reResponseData().addResponseProperty("id")
        gsApiActionDefinition.reResponseData().addResponseProperty("name")
        gsApiActionDefinition.addRelationalEntityResponse("todo")
        gsApiActionDefinition.reResponseData().addResponseProperty("id")
        gsApiActionDefinition.reResponseData().addResponseProperty("name")
        gsApiActionDefinition.addToWhereFilterProperty("id").enableTypeCast()
        return gsApiActionDefinition
    }

    GsApiActionDefinition create(){
        GsApiActionDefinition gsApiActionDefinition = new GsApiActionDefinition<Steps>(Steps)
        gsApiActionDefinition.addRequestProperty("name").required().setErrorMessage("Please Enter Step Name.")
        gsApiActionDefinition.addRequestProperty("description")
        gsApiActionDefinition.addRequestProperty("reference")
        gsApiActionDefinition.addRequestProperty("estimatedHour")
        gsApiActionDefinition.addRequestProperty("status")
        gsApiActionDefinition.addRequestProperty("jsonData")
        gsApiActionDefinition.addRequestProperty("otherInfo")
        gsApiActionDefinition.addRequestProperty("complexity", SwaggerConstant.SWAGGER_DT_LONG)
                .setAlias("complexityId").required()
                .enableTypeCast().setErrorMessage("Required Complexity Information Missing.")
        gsApiActionDefinition.addRequestProperty("todo", SwaggerConstant.SWAGGER_DT_LONG)
                .setAlias("todoId").required()
                .enableTypeCast().setErrorMessage("Required Todo Information Missing.")
        gsApiActionDefinition.requestPreProcessor = new RequestPreProcessor() {
            @Override
            GsFilteredData process(GsApiActionDefinition definition, GsFilteredData gsFilteredData) {
                GrailsParameterMap filteredGrailsParameterMap = gsFilteredData.gsParamsPairData.filteredGrailsParameterMap
                gsFilteredData.gsParamsPairData.addToParams("sortOrder", stepService.getAllStepByComplexityId(filteredGrailsParameterMap.complexity).size())
                return gsFilteredData
            }
        }
        gsApiActionDefinition.successResponseAsData()
        return gsApiActionDefinition
    }

    GsApiActionDefinition update(){
        GsApiActionDefinition gsApiActionDefinition = new GsApiActionDefinition<Steps>(Steps)
        gsApiActionDefinition.addRequestProperty("name")
        gsApiActionDefinition.addRequestProperty("description")
        gsApiActionDefinition.addRequestProperty("status")
        gsApiActionDefinition.addRequestProperty("estimatedHour").setErrorMessage("Please Enter valid Estimation, in Hour format. (1 or 1.5)")
        gsApiActionDefinition.addRequestProperty("startedMoment").enableTypeCast().setDateFormat("yyyy-MM-dd")
        gsApiActionDefinition.addToWhereFilterProperty("id").enableTypeCast()
        gsApiActionDefinition.allowedConditionOnlyEqual()
        gsApiActionDefinition.successResponseAsData()
        return gsApiActionDefinition
    }


    GsApiActionDefinition delete(){
        GsApiActionDefinition gsApiActionDefinition = new GsApiActionDefinition<Steps>(Steps)
        gsApiActionDefinition.addToWhereFilterProperty("id").enableTypeCast()
        gsApiActionDefinition.successResponseFormat = GsApiResponseData.successMessage("Successfully Deleted")
        gsApiActionDefinition.failedResponseFormat = GsApiResponseData.failed("Unable to Delete")
        return gsApiActionDefinition
    }

    GsApiActionDefinition saveSort() {
        GsApiActionDefinition gsApiActionDefinition = new GsApiActionDefinition<Steps>(Steps)
        SwaggerHelper swaggerHelper = new SwaggerHelper()
        swaggerHelper.initItem(SwaggerConstant.SWAGGER_DT_OBJECT, SwaggerConstant.IN_BODY)
        swaggerHelper.addProperties("dbId", SwaggerConstant.SWAGGER_DT_INTEGER)
        swaggerHelper.addProperties("index", SwaggerConstant.SWAGGER_DT_INTEGER)
        gsApiActionDefinition.addRequestProperty("itemMap", SwaggerConstant.SWAGGER_DT_ARRAY_MAP).setPropertyMap(swaggerHelper.getAllProperties())
        gsApiActionDefinition.customProcessor = new CustomProcessor() {
            @Override
            GsApiResponseData process(GsApiActionDefinition actionDefinition, GsParamsPairData paramData, ApiHelper apiHelper) {
                return stepService.saveSorting(actionDefinition, paramData, apiHelper)
            }
        }
        return gsApiActionDefinition
    }
}