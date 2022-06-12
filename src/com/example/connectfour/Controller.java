package com.example.connectfour;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Controller implements Initializable{
	private static final  int COLUMNS = 7;
	private static final  int ROWS = 6;
	private static  final int CIRCLE_DIAMETER = 80;
	private  static final String discColor1 = "#24303E";
	private static  final String getDiscColor2 = "#4CAA88";

	private static String PLAYER_ONE = "Player One";
	private static  String PLAYER_TWO = "Player Two";

	private boolean isPlayerOneTurn = true;

	private Disc[][] insertedDiscsArrays = new Disc[ROWS][COLUMNS];    // For Structural changes : For Developers
	@FXML
	public GridPane rootGridPane;
	@FXML
	public Pane insertedDiscPane;
	@FXML
	public Label labelPlayer;

	private boolean isAllowedInsert  = true; // Flag to avoid same disc at multiple times at a time

	public TextField Playerone;

	public TextField Playerto;

	public Button setNamesOn;

	public void createPlayground(){
		Shape rectangleWithHoles = createGameStructuralGrid();

				rootGridPane.add(rectangleWithHoles, 0,1);

				List<Rectangle> rectangleList = createClickableColumns();

		for (Rectangle rectangle : rectangleList)
		      {
			      rootGridPane.add(rectangle , 0, 1);
		}

		setNamesOn.setOnAction(event -> {
			labelPlayer.setText(isPlayerOneTurn? Playerone.getText():Playerto.getText());
		});
	}

	private Shape  createGameStructuralGrid(){

		Shape rectangleWithHoles = new Rectangle((COLUMNS + 1) * CIRCLE_DIAMETER, (ROWS + 1) * CIRCLE_DIAMETER);

		for (int row = 0; row < ROWS; row++){
			for(int col = 0; col < COLUMNS; col++){
				Circle circle = new Circle();
				circle.setRadius(CIRCLE_DIAMETER / 2);
				circle.setCenterX(CIRCLE_DIAMETER/2);
				circle.setCenterY(CIRCLE_DIAMETER/2);
circle.setSmooth(true);


				circle.setTranslateX(col * (CIRCLE_DIAMETER + 5) + CIRCLE_DIAMETER / 4);
				circle.setTranslateY(row * (CIRCLE_DIAMETER + 5) + CIRCLE_DIAMETER / 4);

				rectangleWithHoles = Shape.subtract(rectangleWithHoles, circle);
			}
		}
		rectangleWithHoles.setFill(Color.WHITE);
		return rectangleWithHoles;
	}

	private List<Rectangle> createClickableColumns() {

	java.util.List<Rectangle> rectangleList = new ArrayList<>();
		for (int col = 0; col < COLUMNS ; col++){

			Rectangle rectangle = new Rectangle(CIRCLE_DIAMETER,(ROWS + 1) * CIRCLE_DIAMETER);
			rectangle.setFill(Color.TRANSPARENT);

			rectangle.setTranslateX(col * (CIRCLE_DIAMETER + 5) + CIRCLE_DIAMETER / 4);

			            rectangle.setOnMouseEntered(event -> rectangle.setFill(Color.valueOf("#eeeeee26")));
			            rectangle.setOnMouseExited(event -> rectangle.setFill(Color.TRANSPARENT));

			            final int column = col;
			            rectangle.setOnMouseClicked(event -> {
			            	if (isAllowedInsert){
			            		isAllowedInsert = false;  // When the disc beaing dropped no more disc is dropped{}Inserted
			            		insertDisc(new Disc(isPlayerOneTurn),column);
				            }


			            });
			                  rectangleList.add(rectangle);
		}

		return rectangleList;
	}

	private  void insertDisc(Disc disc , int column){

		int row = ROWS - 1;
		while (row >= 0){
			if (getDiscPresent(row , column) == null)

			break;
			row--;
		}

		if (row<0){         // If the player was imserted a disc in fully inserted row ,
			                            // then it simply return nothing i.e we can noy=t insert a disc //
			return;
		}

		insertedDiscsArrays[row][column] = disc;    // Structural changes for Developers //
		insertedDiscPane.getChildren().add(disc);  // visual changes for players //
		disc.setTranslateX(column * (CIRCLE_DIAMETER + 5) + CIRCLE_DIAMETER / 4);  // Move the disc from via X axis

		int currentRow = row;
		TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(0.5) , disc);
		translateTransition.setToY(row * (CIRCLE_DIAMETER + 5) + CIRCLE_DIAMETER / 4);
		translateTransition.setOnFinished(event -> {
isAllowedInsert = true;
			if(gameEnded(currentRow , column)){
gameOver();
return;
			}
			isPlayerOneTurn = !isPlayerOneTurn;

			labelPlayer.setText(isPlayerOneTurn? Playerone.getText() : Playerto.getText());
		});
		translateTransition.play();
	}

	private boolean gameEnded(int row, int column){

		List<Point2D> verticalPoints = IntStream.rangeClosed(row - 3 , row + 3) //---> range of ROW Values : 0 1 2 3 4 5 ;

				.mapToObj(r -> new Point2D(r, column)) //--->  Index Of Each Elements Present in
				                                               // column[Column][row] : 0,3 1,3 2,3 3,3 4,3 5,3 Points2D x,y //
				.collect(Collectors.toList()); //--->

		//Horizantal combinations for alla winner

		List<Point2D> horizantalPoints = IntStream.rangeClosed(column - 3 , column + 3)
				.mapToObj(col -> new Point2D(row, col)).collect(Collectors.toList());

		//Diagonal Combinations for winner

Point2D startPoint1 = new Point2D(row -3, column + 3);
List<Point2D> diagonalPoints = IntStream.rangeClosed(0,6)
		                    .mapToObj(i -> startPoint1.add(i, -i)).collect(Collectors.toList());

		Point2D startPoint2 = new Point2D(row -3, column - 3);
		List<Point2D> diagonal2Points = IntStream.rangeClosed(0,6)
				.mapToObj(i -> startPoint2.add(i, i)).collect(Collectors.toList());


		boolean isEnded = checkCombinations(verticalPoints) || checkCombinations(horizantalPoints) || checkCombinations(diagonalPoints)
				|| checkCombinations(diagonal2Points);
		return isEnded;
	}

	private boolean checkCombinations(List<Point2D> points){

		int chain = 0;
		for (Point2D point: points)
		      {
			int rowIndexForArray = (int) point.getX();
			int columnIndexForArray = (int) point.getY();

			Disc disc = getDiscPresent(rowIndexForArray, columnIndexForArray);
			if(disc != null && disc.isPlayerOneMove == isPlayerOneTurn){
				chain++;
				if(chain == 4){
					return true;
				}
			}else {
				chain = 0;
			}
		}
		return false;
	}

	private Disc getDiscPresent(int row , int column) {
		if (row >= ROWS || row < 0 || column >= COLUMNS || column < 0)
			return null;
		return insertedDiscsArrays[row][column];

	}

	private void gameOver(){
String winner = isPlayerOneTurn ? PLAYER_ONE : PLAYER_TWO;
		System.out.println("Winner is "+winner);
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle("Connect Four");
			alert.setHeaderText("The Winner is "+winner);
		alert.setContentText("Are You Want Play again?");
		ButtonType ysButton = new ButtonType("Yes");
		ButtonType noBtn = new ButtonType("No, Exit");
		alert.getButtonTypes().setAll(ysButton, noBtn);

		Platform.runLater( () -> {

			Optional<ButtonType> btnClicked = alert.showAndWait();
			if (btnClicked.isPresent() && btnClicked.get() == ysButton){
				resetGame();
			}else {
				Platform.exit();
				System.exit(0);
			}

		});
	}

public void resetGame() {

		insertedDiscPane.getChildren().clear();  // Remove all the dics from the Pane ---> VIsually

		for (int row = 0; row < insertedDiscsArrays.length ; row++){    // Remive the all the elements Structurally
			for (int col = 0; col < insertedDiscsArrays.length; col++){
				insertedDiscsArrays[row][col] = null;
			}
		}
		isPlayerOneTurn = true; // lets set the player start game
		labelPlayer.setText(PLAYER_ONE);

		createPlayground();  // its simply call the createPlayeGround() method --->  create playground freshly
	}

	private static  class Disc extends Circle{
		private final boolean isPlayerOneMove;

		public Disc (boolean isPlayerOneMove){
			this.isPlayerOneMove = isPlayerOneMove;
			setRadius(CIRCLE_DIAMETER / 2);
			setFill(isPlayerOneMove? Color.valueOf(discColor1): Color.valueOf(getDiscColor2));
			setCenterX(CIRCLE_DIAMETER / 2);
			setCenterY(CIRCLE_DIAMETER / 2);

		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

	}
}
