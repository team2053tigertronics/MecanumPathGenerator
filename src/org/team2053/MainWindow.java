package org.team2053;

import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.ResourceBundle;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

public class MainWindow {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;
    
    @FXML
    private MenuBar menuBar;
    @FXML
    private TableView<RobotPose> tableView;
    @FXML
    private TableColumn<RobotPose, Double> xposCol;
    @FXML
    private TableColumn<RobotPose, Double> yposCol;
    @FXML
    private TableColumn<RobotPose, Double> headingCol;
    @FXML
    private TextField timestepBox;
    @FXML
    private TextField totaltimeBox;
    @FXML
    private TextField robotwidthBox;
    @FXML
    private TextField robotlengthBox;
    @FXML
    private ScatterChart<Number, Number> positionGraph;
    @FXML
    private NumberAxis xAxisPos;
    @FXML
    private NumberAxis yAxisPos;
    @FXML
    private AnchorPane anchorPane;
    
    private final ObservableList<RobotPose> data = FXCollections.observableArrayList(new RobotPose(0,0,0));
    private static MecanumPathPlanner pathPlanner;
    private static XYChart.Series<Number, Number> robotSmoothedSeries;
    private static XYChart.Series<Number, Number> robotPositionSeries;
        
    @FXML
    void handleAddPoint(ActionEvent event) {
    	data.add(new RobotPose(0, 0, 0));
    	tableView.refresh();
    }
    
    @FXML
    void handleDeletePoint(ActionEvent event) {
    	if(data.size() > 0) {
        	data.remove(data.size() - 1);
    	}
    	tableView.refresh();
    }
    
    @FXML
    void handleGenerateCSV(ActionEvent event) {
    	double[][] path = new double[data.size()][3];
    	int index = 0;
    	for(RobotPose pose : data) {
    		path[index][0] = pose.getXPos();
    		path[index][1] = pose.getYPos();
    		path[index][2] = pose.getHeading();
    		index++;
    	}
		pathPlanner = new MecanumPathPlanner(path);
		pathPlanner.calculate(Double.parseDouble(totaltimeBox.getText()), Double.parseDouble(timestepBox.getText()), Double.parseDouble(robotwidthBox.getText()), Double.parseDouble(robotlengthBox.getText()));
		updateGraph();
		pathPlanner.writeSmoothCSV("smoothPath.csv");
		pathPlanner.writeOriginalCSV("originalPath.csv");
    }
    
    void updateGraph() {
    	positionGraph.getData().clear();
    	robotSmoothedSeries.getData().clear();
    	for(double[] arr : pathPlanner.smoothPath) {
    		robotSmoothedSeries.getData().add(new XYChart.Data<Number, Number>(arr[0], -arr[1]));
    	}
    	for(double[] arr : pathPlanner.origPath) {
    		robotPositionSeries.getData().add(new XYChart.Data<Number, Number>(arr[0], -arr[1]));
    	}
    	positionGraph.getData().add(robotSmoothedSeries);
    	positionGraph.getData().add(robotPositionSeries);
    }
    
    @FXML
    void handleConvert(ActionEvent event) {
    	for(RobotPose pose : data) {
    		pose.setXPos(pose.getXPos() * 12);
    		pose.setYPos(pose.getYPos() * 12);
    	}
    	tableView.refresh();
    }
    
    @FXML
    void initialize() {
    	robotSmoothedSeries = new XYChart.Series<Number, Number>();
    	robotSmoothedSeries.setName("Smoothed Position");
    	
    	robotPositionSeries = new XYChart.Series<Number, Number>();
    	robotPositionSeries.setName("Original Position");
    	
    	xAxisPos.setAutoRanging(false);
    	xAxisPos.setLowerBound(0);
    	xAxisPos.setUpperBound(648);
    	xAxisPos.setTickUnit(12);
    	xAxisPos.setMinorTickVisible(false);
    	
    	yAxisPos.setAutoRanging(false);
    	yAxisPos.setLowerBound(-324);
    	yAxisPos.setUpperBound(0);
    	yAxisPos.setTickUnit(12);
    	yAxisPos.setMinorTickVisible(false);
    	
    	yAxisPos.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxisPos) {
    		@Override
    		public String toString(Number value) {
    			return String.format("%7.1f", -value.doubleValue());
    		}
    	});
    	
		xposCol.setCellValueFactory(new PropertyValueFactory<RobotPose, Double>("xPos"));
		xposCol.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<Double>() {

			@Override
			public Double fromString(String arg0) {
				return Double.parseDouble(arg0);
			}

			@Override
			public String toString(Double arg0) {
				return arg0.toString();
			}
			
		}));
		xposCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<RobotPose,Double>>() {
			
			@Override
			public void handle(CellEditEvent<RobotPose, Double> arg0) {
				arg0.getTableView().getItems().get(arg0.getTablePosition().getRow()).setXPos(arg0.getNewValue());
			}
		});
		yposCol.setCellValueFactory(new PropertyValueFactory<RobotPose, Double>("yPos"));
		yposCol.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<Double>() {

			@Override
			public Double fromString(String arg0) {
				return Double.parseDouble(arg0);
			}

			@Override
			public String toString(Double arg0) {
				return arg0.toString();
			}
			
		}));
		yposCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<RobotPose,Double>>() {
			
			@Override
			public void handle(CellEditEvent<RobotPose, Double> arg0) {
				arg0.getTableView().getItems().get(arg0.getTablePosition().getRow()).setYPos(arg0.getNewValue());
			}
		});
		headingCol.setCellValueFactory(new PropertyValueFactory<RobotPose, Double>("heading"));
		headingCol.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<Double>() {

			@Override
			public Double fromString(String arg0) {
				return Double.parseDouble(arg0);
			}

			@Override
			public String toString(Double arg0) {
				return arg0.toString();
			}
			
		}));
		headingCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<RobotPose,Double>>() {
			
			@Override
			public void handle(CellEditEvent<RobotPose, Double> arg0) {
				arg0.getTableView().getItems().get(arg0.getTablePosition().getRow()).setHeading(arg0.getNewValue());
			}
		});
		
		data.clear();
		data.add(new RobotPose(2, 8, 90));
		data.add(new RobotPose(7, 2, 135));
		data.add(new RobotPose(12, 8, 180));
		data.add(new RobotPose(17, 14, 225));
		data.add(new RobotPose(22, 8, 270));
		data.add(new RobotPose(17, 2, 315));
		data.add(new RobotPose(12, 8, 360));
		data.add(new RobotPose(7, 14, 450));
		data.add(new RobotPose(2, 8, 450));

		menuBar.getMenus().get(0).getItems().get(1).setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				String currentDir = System.getProperty("user.dir") + File.separator;				
				FileChooser fc = new FileChooser();
				fc.setTitle("Choose a path to open");
				fc.setInitialDirectory(new File(currentDir));
				File selectedFile = fc.showOpenDialog(menuBar.getScene().getWindow());
				readFile(selectedFile);
			}
		});
		
		tableView.setItems(data);
		tableView.setEditable(true);
    }
    
    public void readFile(File selectedFile){
    	positionGraph.getData().clear();
    	robotSmoothedSeries.getData().clear();
    	robotPositionSeries.getData().clear();
    	data.clear();
    	
    	try {
        	FileReader reader = new FileReader(selectedFile);
        	Iterable<CSVRecord> records = CSVFormat.DEFAULT.parse(reader);	
        	for(CSVRecord record : records) {
        		System.out.println("record: " + record.toString());
        		RobotPose pose = new RobotPose(0,0,0);
        		pose.setXPos(Double.parseDouble(record.get(0)));
        		pose.setYPos(Double.parseDouble(record.get(1)));
        		pose.setHeading(Double.parseDouble(record.get(2)));
        		data.add(pose);
        	}
    	}
    	catch(Exception e) {
    		System.err.println(e.getMessage());
    	}
    }
}
