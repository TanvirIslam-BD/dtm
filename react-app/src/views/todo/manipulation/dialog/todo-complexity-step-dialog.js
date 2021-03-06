import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogTitle from '@material-ui/core/DialogTitle';
import PropTypes from "prop-types";
import {
    Button, TextField, Grid, MenuItem
} from '@material-ui/core'
import React from 'react';
import RaViewComponent from "../../../../artifacts/ra-view-component";
import {RaGsConditionMaker} from "../../../../artifacts/ra-gs-condition-maker";
import {ApiURL} from "../../../../app/api-url";
import RaMarkdown from "../../../../artifacts/ra-markdown";
import {TaskStatusColor} from "../../../../app/task-status-color";

export default class TodoComplexityStepDialog extends RaViewComponent {


    constructor(props) {
        super(props);
        this.state = {
            formTitle: "Add New ",
            formButton: "Save",
            markDownDisplayField: "description",
            formData: {},
            formError: {},
            status: {}
        };
    }

    componentDidMount() {
        this.showFlashMessage();
        this.setState((state) => {
            let formData = {
                status: "DRAFT"
            };
            return {formData: formData};
        });
        this.loadDropDownValues();
        this.loadEditData();
    }

    loadDropDownValues(){
        this.getToApi(ApiURL.CommonDropDownConstant,  response => {
            let data = response.data.response;
            if (data.status){
                this.setState({status: data.status})
            }
        });
    }

    closePopup = () => {
        const { parent } = this.props;
        parent.setState({isOpenTodoComplexityStep: false});
    };


    loadEditData() {
        const { editId, parent } = this.props;
        if (editId !== undefined) {
            this.setState(state => ({
                formTitle: "Edit ",
                formButton: "Update",
            }));
            let condition = RaGsConditionMaker.equal({}, "id", editId);
            this.postJsonToApi(ApiURL.StepDetails, condition, response => {
                let data = response.data;
                if(data.isSuccess){
                    this.setState({formData: response.data.response});
                }else{
                    parent.showErrorInfo(data.message);
                    this.closePopup();
                }
            }, failed =>{
                parent.showErrorInfo("Unable to Open Edit Form.");
                this.closePopup();
            });
        }
    }

    formSubmitHandler = event => {
        event.preventDefault();
        const { parent, editId, complexityId,  todoId} = this.props;
        let formData = this.state.formData;
        let url = ApiURL.StepCreate;
        let successMessage = "Successfully Added Step!!";
        if (editId !== undefined){
            url = ApiURL.StepUpdate;
            successMessage = "Successfully Updated Step!!";
            formData = RaGsConditionMaker.equal(formData, "id", Number(editId))
        }else{
            formData.todoId = todoId;
            formData.complexityId = complexityId;
        }

        this.postJsonToApi(url, formData, success => {
                let data = success.data;
                if (data.isSuccess){
                    parent.loadComplexityStep(
                        ()=>{
                            this.closePopup();
                            parent.showSuccessInfo(successMessage);
                        }
                    );
                }else{
                    this.showErrorInfo(data.message);
                    this.responseErrorProcessor(data);
                }
            }
        )
    };

    selectMarkdownViewer (event, name){
        this.setState({markDownDisplayField: name});
    }

    loadComplexityStep(callBack) {
        this.getToApi(ApiURL.StepGetDetailsByTodo + "?todoId=" + this.state.allDetails.id, response => {
            let responseData = response.data.response;
            if (responseData){
                this.setState({complexityAndSteps: responseData});
            }
            if (callBack){
                callBack();
            }
        });
    }

    appRender() {
        const {classes, uiDefinition} = this.props;
        return (
            <Dialog open={true}>
                <DialogTitle>{this.state.formTitle}{uiDefinition.complexityStepPanelTitle}</DialogTitle>
                <DialogContent>
                    <form onSubmit={this.formSubmitHandler}>
                        <Grid container spacing={8}>
                            <Grid item xs={4}>
                                <TextField {...this.onChangeSelectProcessor("status")} label="Status" select fullWidth>
                                    {
                                        Object.entries(this.state.status).map(([objectKey, objectValue], key) => {
                                            return (<MenuItem key={key} value={objectKey}>{TaskStatusColor.statusOnText(objectValue, objectKey)}</MenuItem>)
                                        })
                                    }
                                </TextField>
                            </Grid>

                            <Grid item xs={4}><TextField label="Started" disabled value={this.getFormDataValue("startedMoment")}  fullWidth/></Grid>
                            <Grid item xs={4}><TextField type="number" label="Estimated Hour" {...this.onChangeTextFieldProcessor("estimatedHour")}   fullWidth/></Grid>

                            <Grid item xs={12}><TextField name="" label="Name" {...this.onChangeTextFieldProcessor("name")} fullWidth/></Grid>
                            <Grid item xs={12}><TextField multiline rows={8} label="Description (Markdown)" {...this.onChangeTextFieldProcessor("description")} onFocus={event =>{this.selectMarkdownViewer(event, "description")}} fullWidth/></Grid>
                            <Grid item xs={12}><TextField multiline rows={8} label="Reference (Markdown)" {...this.onChangeTextFieldProcessor("reference")} onFocus={event =>{this.selectMarkdownViewer(event, "reference")}} fullWidth/></Grid>

                            <Grid item xs={12}>
                                <RaMarkdown content={this.getFormDataValue(this.state.markDownDisplayField)}/>
                            </Grid>

                        </Grid>
                        <DialogActions>
                            <Button type="submit" color="primary" variant="raised">{this.state.formButton}</Button>
                            <Button onClick={this.closePopup} color="primary" variant="raised">Cancel</Button>
                        </DialogActions>
                    </form>
                </DialogContent>
            </Dialog>
        );
    }
}


TodoComplexityStepDialog.propTypes = {
    parent: PropTypes.object.isRequired,
    editId: PropTypes.number,
    complexityId: PropTypes.number,
    todoId: PropTypes.number,
    uiDefinition: PropTypes.object.isRequired,
};

