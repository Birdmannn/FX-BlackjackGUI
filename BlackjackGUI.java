package com.example.blackkackgui;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.layout.HBox;
import javafx.scene.image.Image;
import javafx.scene.canvas.Canvas;




public class BlackjackGUI extends Application {

    private Canvas board;
    private Image cardImages;
    private Deck deck;
    private boolean gameInProgress, newGame;
    private BlackjackHand userHand;
    private BlackjackHand dealerHand;
    private Button hitButton, standButton, newGameButton;
    private String message;
    private TextField betInput;
    private int hit;
    private int bet, balance = 100;
    private GraphicsContext g;

    public void start(Stage stage) {

        cardImages = new Image("cards.png");
        board = new Canvas(5*99 + 20, 2*123 + 120 + 40);
        g = board.getGraphicsContext2D();
        g.setFill(Color.GREEN);
        g.fillRect(0,0,board.getWidth(), board.getHeight());
        g.setFill(Color.WHITE);
        g.setFont(Font.font("Consolas",20));
        g.fillText("Welcome to Blackjack",board.getWidth()/4+20, board.getHeight()/2);
        g.setFont(Font.font(14));
        g.setFill(Color.LIGHTGRAY);
        g.fillText("Current Balance: $100",20, board.getHeight()-40);
        g.fillText("Place your bet.",20, board.getHeight()-20);

        hitButton = new Button("Hit!");
        standButton = new Button("Stand!");
        newGameButton = new Button("New Game");

        betInput = new TextField("0");
        betInput.setPrefColumnCount(5);

        HBox buttonBar = new HBox(5,hitButton,standButton,newGameButton, new Label("Your bet:"), betInput);
        buttonBar.setAlignment(Pos.CENTER);
        buttonBar.setStyle("-fx-background-color: #fffdd0; -fx-padding: 4px");


        hitButton.setOnAction(e -> {
            hit = 1;
            drawBoard();
        });
        standButton.setOnAction(e -> {
            hit = 2;
            drawBoard();
        });
        newGameButton.setOnAction(e -> doNewGame());

        VBox root = new VBox(2,board,buttonBar);
        root.setStyle("-fx-background-color: brown; -fx-border-width: 2px; -fx-border-color: brown");


        doNewGame();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Simple Casino Blackjack Game");
        stage.setResizable(false);
        stage.show();



    }


    public int betUpdate() {
        try {
            String str = betInput.getText();
            if(str.equals(""))
                bet = -1;
            else
                bet = Integer.parseInt(str);
        }
        catch(NumberFormatException e) {
            betInput.requestFocus();
            betInput.selectAll();
        }
        return bet;
    }


    public void doNewGame() {
        gameEnded(true);

        if(betUpdate() <= 0 || betUpdate() > balance) {
            betInput.requestFocus();
            betInput.selectAll();
        }
        else {
            deck = new Deck();
            userHand = new BlackjackHand();
            dealerHand = new BlackjackHand();
            deck.shuffle();
            gameEnded(false);
            newGame = true;
            drawBoard();


        }
    }

    public void drawBoard() {

        g.setFill(Color.GREEN);
        g.fillRect(0,0,board.getWidth(), board.getHeight());
        g.setFont(Font.font(14));
        g.setFill(Color.WHITE);
        g.fillText("Dealer's Cards:",20,30);
        g.fillText("Your Cards:", 20,40+123+30);
        g.setFill(Color.YELLOW);

        playGame();
        if(newGame && balance == 0)
            message = "Insufficient Balance.";
        else
            message = "Current Balance: $" + (balance);

        g.fillText(message,20, board.getHeight()-40);

        if(gameInProgress) {
            drawCard(dealerHand.getCard(0),20,40);
            drawCard(null,20+99,40);
            message = "You have " + userHand.getBlackjackValue() + ". Hit or Stand?";
            g.fillText(message, 20, board.getHeight()-20);

        }
        else {
            for(int i = 0; i < dealerHand.getCardCount(); i++) {
                drawCard(dealerHand.getCard(i), 20+i*99,40);
            }
        }
        for(int i = 0; i < userHand.getCardCount(); i++) {
            drawCard(userHand.getCard(i),20+i*99, 40+123+40);
        }


    }

    public void playGame() {
        if (newGame) {
            balance -= bet;

            gameInProgress = true;
            for (int i = 0; i < 2; i++) {
                userHand.addCard(deck.dealCard());
                dealerHand.addCard(deck.dealCard());
            }
            if (dealerHand.getBlackjackValue() == 21) {
                gameEnded(true);
                gameInProgress = false;
                fillMessage(false);
            } else if (dealerHand.getBlackjackValue() > 21) {
                gameEnded(true);
                gameInProgress = false;
                fillMessage(true);

            }
            newGame = false;
        } else if (hit == 1) {

            userHand.addCard(deck.dealCard());
            if (userHand.getBlackjackValue() > 21) {
                gameEnded(true);
                gameInProgress = false;
                fillMessage(false);
            }
        } else if (hit == 2) {
            if (dealerHand.getBlackjackValue() < 16) {
                do {
                    dealerHand.addCard(deck.dealCard());
                } while (dealerHand.getBlackjackValue() <= 16);
            }

            gameInProgress = false;

            fillMessage(dealerHand.getBlackjackValue() > 21 || dealerHand.getBlackjackValue() < userHand.getBlackjackValue());
            gameEnded(true);
        }
        if (balance == 0) {
            newGameButton.setDisable(true);
            betInput.setEditable(false);
        }
    }

    public void fillMessage(boolean t) {

        g.setFill(Color.YELLOW);
        g.setFont(Font.font(14));
        if(t) {
            message = "You won.";
            balance = balance + bet + bet;
        }
        else {
            message = "You lost to the dealer.";
        }

        g.fillText(message,20,board.getHeight()-20);
        betInput.clear();
        betInput.requestFocus();

    }

    public void gameEnded(boolean t) {
        if(t) {
           hitButton.setDisable(true);
           standButton.setDisable(true);
           newGameButton.setDisable(false);
           betInput.setEditable(true);
        }
        else {
            hitButton.setDisable(false);
            standButton.setDisable(false);
            newGameButton.setDisable(true);
            betInput.setEditable(false);

        }

    }

    public void drawCard(Card card, int x, int y) {
        int cardRow, cardCol;
        if(card == null) {
            cardRow = 4;
            cardCol = 2;
        }
        else {
            cardRow = 3 - card.getSuit();
            cardCol = card.getValue() - 1;
        }
        double sx, sy;
        sx = 79 * cardCol;
        sy = 123 * cardRow;

        g.drawImage(cardImages,sx,sy,79,123,x,y,79,123);
    }


    public static void main(String[] args) {
        launch();
    }
}