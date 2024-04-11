package com.blackjack.client;

import com.blackjack.game.Card;
import com.blackjack.game.GameState;
import com.blackjack.game.Player;
import com.blackjack.network.Message;
import com.blackjack.network.MessageType;
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
import java.util.Objects;
import java.util.Optional;

public class BlackjackGUI extends Application {
    private BlackjackClient client;
    private Stage stage;
    private Label messageLabel;
    private Label balanceLabel;
    private TextField betTextField; // Declare betTextField as an instance variable
    private Button hitButton;
    private Button standButton;
    private HBox playerCardsBox;
    private HBox dealerCardsBox;
    private TextArea chatArea;
    private TextField chatTextField;

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        stage.setTitle("Blackjack");

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.CENTER);

        HBox playerBoxes = new HBox(10);
        playerBoxes.setAlignment(Pos.CENTER);

        VBox dealerBox = new VBox(10);
        dealerBox.setAlignment(Pos.CENTER);
        Label dealerLabel = new Label("Dealer");
        dealerCardsBox = new HBox(10);
        dealerBox.getChildren().addAll(dealerLabel, dealerCardsBox);

        for (int i = 0; i < 4; i++) {
            VBox playerBox = createPlayerBox(i);
            playerBoxes.getChildren().add(playerBox);
        }

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

        root.getChildren().addAll(dealerBox, playerBoxes, chatArea, chatBox);

        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.show();

        // Connect to the server
        client = new BlackjackClient("localhost", 8888, this);
        client.start();
    }

    private VBox createPlayerBox(int index) {
        VBox playerBox = new VBox(10);
        playerBox.setAlignment(Pos.CENTER);

        Label playerLabel = new Label("Player " + (index + 1));
        Label balanceLabel = new Label("Balance: $2000");

        betTextField = new TextField(); // Initialize betTextField
        betTextField.setPromptText("Enter bet amount");

        Button betButton = new Button("Place Bet");
        betButton.setOnAction(e -> placeBet());

        Button hitButton = new Button("Hit");
        hitButton.setOnAction(e -> hit());

        Button standButton = new Button("Stand");
        standButton.setOnAction(e -> stand());

        HBox cardsBox = new HBox(10);

        playerBox.getChildren().addAll(playerLabel, balanceLabel, betTextField, betButton, hitButton, standButton, cardsBox);

        return playerBox;
    }

    private void placeBet() {
        String betAmountString = betTextField.getText().trim();
        if (!betAmountString.isEmpty()) {
            try {
                int betAmount = Integer.parseInt(betAmountString);
                client.sendMessage(new Message(MessageType.PLACE_BET, betAmount));
            } catch (NumberFormatException e) {
                // Display an error message or take appropriate action
                System.out.println("Invalid bet amount. Please enter a valid integer value.");
            }
        } else {
            // Display an error message or take appropriate action
            System.out.println("Please enter a bet amount.");
        }
    }

    private void hit() {
        client.sendMessage(new Message(MessageType.PLAYER_ACTION, "HIT"));
    }

    private void stand() {
        client.sendMessage(new Message(MessageType.PLAYER_ACTION, "STAND"));
    }

    private void sendChatMessage() {
        String message = chatTextField.getText();
        if (!message.isEmpty()) {
            client.sendMessage(new Message(MessageType.CHAT_MESSAGE, message));
            chatTextField.clear();
        }
    }

    public void updateGameState(GameState gameState) {
        Platform.runLater(() -> {
            Player player = gameState.getPlayer(client.getPlayerName());
            balanceLabel.setText("Balance: $" + player.getBalance());

            playerCardsBox.getChildren().clear();
            for (Card card : player.getHand().getCards()) {
                playerCardsBox.getChildren().add(createCardImageView(card));
            }

            dealerCardsBox.getChildren().clear();
            List<Card> dealerCards = gameState.getDealer().getHand().getCards();
            for (int i = 0; i < dealerCards.size(); i++) {
                if (i == 0 && !gameState.isGameOver()) {
                    ImageView backImageView = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/back.png"))));
                    backImageView.setFitWidth(95);
                    backImageView.setFitHeight(130);
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
                messageLabel.setText("");
                Player currentPlayer = gameState.getCurrentPlayer();
                if (currentPlayer != null && currentPlayer.getName().equals(client.getPlayerName())) {
                    hitButton.setDisable(false);
                    standButton.setDisable(false);
                } else {
                    hitButton.setDisable(true);
                    standButton.setDisable(true);
                }
            }
        });
    }

    private ImageView createCardImageView(Card card) {
        String rank = card.getRank().toLowerCase();
        if (rank.equals("10")) {
            rank = "ten";
        }
        String imagePath = "/images/" + rank + "_of_" + card.getSuit().toLowerCase() + ".png";
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath)));
        if (image.isError()) {
            System.err.println("Error loading image: " + imagePath);
            return new ImageView(); // Return an empty ImageView if the image cannot be loaded
        }
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(95);
        imageView.setFitHeight(130);
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

    public void addMessage(String message) {
        chatArea.appendText(message + "\n");
    }

    String getPlayerName() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Player Name");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter your name:");

        Optional<String> result = dialog.showAndWait();
        return result.orElse("Player");
    }

    public static void main(String[] args) {
        launch(args);
    }
}