package com.rspsi;

import java.io.IOException;
import java.util.List;

import org.apache.commons.compress.utils.Lists;

import com.jagex.chunk.Chunk;
import com.rspsi.controls.SelectFilesNode;
import com.rspsi.resources.ResourceLoader;
import com.rspsi.util.FXUtils;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class SelectFilesWindow extends Application {

	private Stage stage;
	private boolean okClicked;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		this.stage = primaryStage;
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/selectfiles.fxml"));
	
		loader.setController(this);
		Parent content = (Parent) loader.load();
		Scene scene = new Scene(content);
		
		
		
		primaryStage.setTitle("Please select files to load");
		primaryStage.initStyle(StageStyle.UTILITY);
		primaryStage.setScene(scene);
		primaryStage.getIcons().add(ResourceLoader.getSingleton().getLogo64());

		primaryStage.setAlwaysOnTop(true);

		widthSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1, 1));
		lengthSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1, 1));
		RowConstraints defaultRow = new RowConstraints();
	
		widthSpinner.getValueFactory().valueProperty().addListener((observable, oldVal, newVal) -> {
	
				if(oldVal < newVal) {//Increase
					List<SelectFilesNode> nodes = generateSelectNodes(FXUtils.getRowCount(gridPane));
					
					gridPane.addColumn(newVal - 1, nodes.toArray(new SelectFilesNode[nodes.size()]));

				} else if(oldVal > newVal) {
					FXUtils.deleteColumn(gridPane, oldVal - 1);
				}

				stage.sizeToScene();
		});
		
		lengthSpinner.getValueFactory().valueProperty().addListener((observable, oldVal, newVal) -> {
	
				if(oldVal < newVal) {//Increase
					List<SelectFilesNode> nodes = generateSelectNodes(FXUtils.getColumnCount(gridPane));
					gridPane.addRow(newVal - 1, nodes.toArray(new SelectFilesNode[nodes.size()]));

				} else if(oldVal > newVal) {

					FXUtils.deleteRow(gridPane, oldVal - 1);
				}
			stage.sizeToScene();
		
		});
		
		try {
			gridPane.add(new SelectFilesNode(stage), 0, 0);
		} catch(Exception ex) {
			
		}

	
		okButton.setOnAction(evt -> {
			primaryStage.hide();
			okClicked = true;
		});
		cancelButton.setOnAction(evt -> {
			reset();
			primaryStage.hide();
		});
	}
	
	private List<SelectFilesNode> generateSelectNodes(int count) {

		List<SelectFilesNode> nodes = Lists.newArrayList();
		for(int i = 0;i<count;i++)
			try {
				nodes.add(new SelectFilesNode(stage));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return nodes;
	}
	public void show() {
		reset();
		stage.sizeToScene();
		okButton.requestFocus();
		stage.showAndWait();
		if(!okClicked)
			reset();
	}
	
	public List<Chunk> prepareChunks(){
		List<Chunk> chunks = Lists.newArrayList();

		int defaultObjId = 0;
		int defaultLandscapeId = 1;
		for(SelectFilesNode selectNode : getSelectNodes()){
			if(!selectNode.valid())
				continue;
			int positionX = GridPane.getColumnIndex(selectNode);
			int positionY =	(FXUtils.getRowCount(gridPane) - 1) - GridPane.getRowIndex(selectNode);
			int hash = (positionX << 8) + positionY;
			Chunk chunk = new Chunk(hash);

			chunk.offsetX = 64 * positionX;
			chunk.offsetY = 64 * positionY;
			
			chunk.objectMapData = selectNode.getObjectMapData();
			chunk.tileMapData = selectNode.getLandscapeMapData();
			chunk.objectMapId = selectNode.tryGetObjectMapId(defaultObjId+=2);
			chunk.tileMapId = selectNode.tryGetLandscapeMapId(defaultLandscapeId+=2);
			chunks.add(chunk);
		}
		
		
		return chunks;
		
	}
	
	private List<SelectFilesNode> getSelectNodes(){
		List<SelectFilesNode> nodes = Lists.newArrayList();
		for(Node node : gridPane.getChildren()){
			if(node instanceof SelectFilesNode) {
				SelectFilesNode selectNode = (SelectFilesNode) node;
				nodes.add(selectNode);
			}
		}
		return nodes;
	}
	
	public void reset() {
		gridPane.getChildren().clear();
		widthSpinner.getValueFactory().setValue(1);
		lengthSpinner.getValueFactory().setValue(1);
		try {
			gridPane.add(new SelectFilesNode(stage), 0, 0);
		} catch(Exception ex) {
			
		}
	}
	
	public boolean valid() {
		boolean valid = true;
		for(SelectFilesNode selectNode : getSelectNodes()){
			if(!selectNode.valid()) {
				valid = false;
				break;
			}

		}
		return valid;
	}


    @FXML
    private Spinner<Integer> widthSpinner;

    @FXML
    private Spinner<Integer> lengthSpinner;

	@FXML 
	private GridPane gridPane;
	
    @FXML
    private Button okButton;

    @FXML
    private Button cancelButton;
}
