package com.blackjack.client;

import com.blackjack.game.Card;
import com.blackjack.game.GameState;
import com.blackjack.game.Player;
import com.blackjack.network.Message;
import com.blackjack.network.MessageType;
import com.blackjack.server.BlackjackServer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;



public class BlackjackGUI extends Application {
    private BlackjackClient client;
    private Stage stage;
    private VBox playerBox;
    private Label messageLabel;
    private HBox playerCardsBox;
    private HBox dealerCardsBox;
    private TextArea chatArea;
    private TextField chatTextField;
    private Button hitButton;
    private Button standButton;
    private String playerName;

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        stage.setTitle("Blackjack");

        // Create start screen
        VBox startScreen = createStartScreen();
        Scene startScene = new Scene(startScreen, 400, 300);
        stage.setScene(startScene);
        stage.show();
    }

    private VBox createStartScreen() {
        VBox startScreen = new VBox(10);
        startScreen.setPadding(new Insets(10));
        startScreen.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Blackjack");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        TextField nameTextField = new TextField();
        nameTextField.setPromptText("Enter your name");
        nameTextField.setMaxWidth(200);

        Button connectButton = new Button("Connect");
        connectButton.setOnAction(e -> {
            playerName = nameTextField.getText().trim();
            if (!playerName.isEmpty()) {
                connectToServer(playerName);
            }
        });

        startScreen.getChildren().addAll(titleLabel, nameTextField, connectButton);
        return startScreen;
    }


    private void connectToServer(String name) {
        client = new BlackjackClient("localhost", 8888, this);
        System.out.println("Player name: " + name); // Debugging statement
        client.setPlayerName(name);

        // Correcting the access to getCurrentState()
        System.out.println("PS2");
        client.getCurrentState().addPlayer(new Player(name)); // Access through instance `client`

        client.start(name);

        // Create main game screen
        VBox gameScreen = createGameScreen();
        Scene gameScene = new Scene(gameScreen, 800, 600);
        stage.setScene(gameScene);
    }


    private VBox createGameScreen() {
        VBox gameScreen = new VBox(10);
        gameScreen.setPadding(new Insets(10));
        gameScreen.setAlignment(Pos.CENTER);

        HBox dealerBox = new HBox(10);
        dealerBox.setAlignment(Pos.CENTER);
        Label dealerLabel = new Label("Dealer");
        dealerCardsBox = new HBox(10);
        dealerBox.getChildren().addAll(dealerLabel, dealerCardsBox);

        playerBox = new VBox(10);
        playerBox.setAlignment(Pos.CENTER);

        // Create a new HBox for the player's name and cards
        HBox playerNameAndCardsBox = new HBox(10);
        playerNameAndCardsBox.setAlignment(Pos.CENTER);

        Label playerLabel = new Label(playerName);
        playerCardsBox = new HBox(10);

        // Add the player's name and cards to the new HBox
        playerNameAndCardsBox.getChildren().addAll(playerLabel, playerCardsBox);

        hitButton = new Button("Hit");
        hitButton.setOnAction(e -> hit());
        standButton = new Button("Stand");
        standButton.setOnAction(e -> stand());
        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(hitButton, standButton);

        // Add the playerNameAndCardsBox to the playerBox
        playerBox.getChildren().addAll(playerNameAndCardsBox, buttonBox);

        messageLabel = new Label();
        messageLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setPrefHeight(100);

        chatTextField = new TextField();
        chatTextField.setPromptText("Enter chat message");
        chatTextField.setOnAction(e -> sendChatMessage());

        Button sendButton = new Button("Send");
        sendButton.setOnAction(e -> sendChatMessage());

        HBox chatBox = new HBox(10);
        chatBox.getChildren().addAll(chatTextField, sendButton);

        gameScreen.getChildren().addAll(dealerBox, playerBox, messageLabel, chatArea, chatBox);
        return gameScreen;
    }

    /*private void placeBet() {
        String betAmountString = betTextField.getText().trim();
        if (!betAmountString.isEmpty()) {
            try {
                int betAmount = Integer.parseInt(betAmountString);
                client.sendMessage(new Message(MessageType.PLACE_BET, betAmount));
            } catch (NumberFormatException e) {
                showAlert("Invalid Bet Amount", "Please enter a valid integer value.");
            }
        } else {
            showAlert("Missing Bet Amount", "Please enter a bet amount.");
        }
    }*/

    private void hit() {
        client.sendMessage(new Message(MessageType.PLAYER_ACTION, "HIT"));
    }

    private void stand() {
        client.sendMessage(new Message(MessageType.PLAYER_ACTION, "STAND"));
    }

    private void sendChatMessage() {
        String message = chatTextField.getText().trim();
        if (!message.isEmpty()) {
            client.sendMessage(new Message(MessageType.CHAT_MESSAGE, message));
            chatTextField.clear();
        }
    }

    public void updateGameState(GameState gameState) {
        System.out.println("PASSED GAMESTATE1: " + gameState.toString());
        Platform.runLater(() -> {
            System.out.println("PASSED GAMESTATE: " + gameState.toString());
            Player player = gameState.getPlayer(client.getPlayerName());
            System.out.println("Player: " + player);
            if (player != null) {
                playerCardsBox.getChildren().clear();
                List<Card> playerCards = player.getHand().getCards();
                System.out.println("Player cards: " + playerCards);
                for (Card card : playerCards) {
                    System.out.println("Player card: " + card);
                    playerCardsBox.getChildren().add(createCardImageView(card));
                }

                dealerCardsBox.getChildren().clear();
                List<Card> dealerCards = gameState.getDealer().getHand().getCards();
                for (int i = 0; i < dealerCards.size(); i++) {
                    if (i == dealerCards.size() - 1 && !gameState.isGameOver()) {
                        ImageView backImageView = new ImageView(new Image(getClass().getResourceAsStream("/images/back.png")));
                        backImageView.setFitWidth(60);
                        backImageView.setFitHeight(80);
                        dealerCardsBox.getChildren().add(backImageView);
                    } else {
                        dealerCardsBox.getChildren().add(createCardImageView(dealerCards.get(i)));
                    }
                }

                if (gameState.isGameOver()) {
                    String result = getGameResult(player, gameState);
                    messageLabel.setText(result);
                    hitButton.setDisable(true);
                    standButton.setDisable(true);
                } else {
                    Player currentPlayer = gameState.getCurrentPlayer();
                    if (currentPlayer != null && currentPlayer.getName().equals(client.getPlayerName())) {
                        hitButton.setDisable(false);
                        standButton.setDisable(false);
                    } else {
                        hitButton.setDisable(true);
                        standButton.setDisable(true);
                    }
                }
            } else {
                System.out.println("Player not found in the game state: " + client.getPlayerName());
            }
        });
    }

    private ImageView createCardImageView(Card card) {
        String rank = card.getRank().toLowerCase();
        String rankForImagePath;

        switch (rank) {
            case "2" :
                rankForImagePath = "two";
                break;
            case "3" :
                rankForImagePath = "three";
                break;
            case "4" :
                rankForImagePath = "four";
                break;
            case "5" :
                rankForImagePath = "five";
                break;
            case "6" :
                rankForImagePath = "six";
                break;
            case "7" :
                rankForImagePath = "seven";
                break;
            case "8" :
                rankForImagePath = "eight";
                break;
            case "9" :
                rankForImagePath = "nine";
                break;
            case "10":
                rankForImagePath = "ten";
                break;
            default:
                rankForImagePath = rank;
        }

        String imagePath = "/images/" + rankForImagePath + "_of_" + card.getSuit().toLowerCase() + ".png";
        System.out.println("Image path: " + imagePath);
        Image image = new Image(getClass().getResourceAsStream(imagePath));
        if (image.isError()) {
            System.err.println("Error loading image: " + imagePath);
            return new ImageView(); // Return an empty ImageView if the image cannot be loaded
        }
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(60);
        imageView.setFitHeight(80);
        return imageView;
    }

    private String getGameResult(Player player, GameState gameState) {
        int playerValue = player.getHand().getValue();
        int dealerValue = gameState.getDealer().getHand().getValue();

        if (playerValue > 21) {
            return "Bust! You lose.";
        } else if (dealerValue > 21) {
            return "Dealer busts! You win.";
        } else if (playerValue > dealerValue) {
            return "You win!";
        } else if (playerValue < dealerValue) {
            return "You lose.";
        } else {
            return "Push.";
        }
    }

    void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void addMessage(String message) {
        chatArea.appendText(message + "\n");
    }

    public static void main(String[] args) {
        launch(args);
    }
}