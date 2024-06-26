package main;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class Game implements Initializable {

	Parent root;
	Scene scene;
	Stage stage;

	@FXML
	private ImageView playerCard1, playerCard2, playerCard3, playerCard4, playerCard5, playerCard6, playerCard7, playerCard8, playerCard9;

	@FXML
	private ImageView playerCardChoiceImage;

	@FXML
	private Label playerLabel;

	@FXML
	private Label playerTurnLabel;

	@FXML
	private MenuItem quitAndSaveMB;

	@FXML
	private MenuItem saveMB;

	@FXML
	private Label roundLabel;

	@FXML
	private ImageView wildCardImage;

	@FXML
	private Label wildEffectsLabel;

	@FXML
	Button confirmCardBtn;

	@FXML
	private Label damageCardChoiceLabel;

	static String code;
	static boolean isNewGame;
	static boolean isTournament;
	static boolean isLoadGame;
	static boolean activateCPU = true;

	// ELEMENTS, FOR CODE READABILITY
	static final Card.Element FIRE = Card.Element.FIRE;
	static final Card.Element WATER = Card.Element.WATER;
	static final Card.Element FOREST = Card.Element.FOREST;

	// CARD DECK AND WILDS DECK
	static Deck deck = new Deck();
	static final ArrayList<Card> wildsDeck = deck.getWildsDeck();

	// PLAYERS, currentPlayer IS USED TO KEEP TRACK OF TURN
	static Player player1;
	static Player player2;
	static Player currentPlayer;

	// PLAYER HANDS, EXTRACTED FROM ABOVE deck VARIABLE
	static ArrayList<Card> player1Hand;
	static ArrayList<Card> player2Hand;

	static int roundNumber;

	// TO MAP CARD IMAGES TO CARD OBJECTS FOR SELECTION
	private final HashMap<ImageView, Card> cardMap = new HashMap<>();

	// PLAYER WILD CARDS OF THE CURRENT & PREVIOUS ROUNDS
	static Card player1WildCard, p1PreviousWildCard;
	static Card player2WildCard, p2PreviousWildCard;

	// THE SELECTED CARD FOR THE CURRENT PLAYER
	private Card selectedCard;

	// FINAL DAMAGE OF CARDS FOR ROUND WINNER DETERMINATION, KEEPS INTO ACCOUNT WILD CARD MULTIPLIERS AND ELEMENTAL ADVANTAGES/DISADVANTAGES
	static double p1FinalDmg = 0, p2FinalDmg = 0;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// NECESSARY IF MORE THAN ONE GAME RAN ON THE SAME INSTANCE
		CreateGameController.CPUCount = 1;
		code = CreateGameController.code;
		if (isNewGame) {
//			code = CreateGameController.code;
//			if (isTournament) {
//				code = TournamentNextGameController.nextGameCode;
//			}
			startNewGame();
		} else if (isLoadGame) {
			code = LoadGameController.code;
//			if (isTournament) {
//				startGameFromSaveTournament(code);
//			} else {
//  			startGameFromSave(code);
//			}
			startGameFromSave(code);
		} else {
			startGameAfterRound();
		}
	}

	/*
	 * Questo metodo permette di cambiare scena, e in base alla scena selezionata, imposta certi parametri relativi per quella scena
	 */
	public void switchToScene(String fxmlFile) throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
		root = loader.load();
		// DEPENDING ON THE SCENE, SET NECESSARY PARAMETERS
		switch (fxmlFile) {
			case "RoundWinner.fxml" -> {
				RoundWinnerController controller = loader.getController();
				controller.setPlayers(player1, player2);
				controller.setRoundNumber(roundNumber);
				if (p1FinalDmg == p2FinalDmg) {
					controller.setRoundWinner(null);
				} else {
					controller.setRoundWinner(selectRoundWinner());
				}
			}
			case "GameWinner.fxml" -> {
				GameWinnerController controller = loader.getController();
				controller.setPlayersDisplay(player1, player2);
				controller.setGameWinner(selectGameWinner());
			}
			case "TournamentNextGame.fxml" -> {
				// GATHERING DATA FOR NEXT GAME AND FOR FINAL TOURNAMENT LEADERBOARD
				TournamentNextGameController controller = loader.getController();
				controller.setPlayersDisplay(player1, player2);
				controller.setGameWinner(selectGameWinner());
				int gameNum = Integer.parseInt(code.substring(code.length() - 1)) + 1;
				TournamentNextGameController.nextGameCode = code.substring(0, code.length() - 1) + gameNum;

				TournamentNextGameController.match2Player1 = CreateGameController.players.getFirst();
				TournamentNextGameController.match2Player2 = CreateGameController.players.getLast();

//				if (code.endsWith("-1")) {
//					TournamentWinnerController.game1Winner = selectGameWinner();
//					TournamentWinnerController.game1WinnerScore = selectGameWinner().playerScore;
//					TournamentWinnerController.game1Loser = selectGameWinner() == player1 ? player2 : player1;
//					TournamentWinnerController.game1LoserScore = TournamentWinnerController.game1Loser.playerScore;
//					TournamentWinnerController.match2Player1 = ;
//				} else if (code.endsWith("-2")) {
//					TournamentWinnerController.game2Winner = selectGameWinner();
//					TournamentWinnerController.game2WinnerScore = selectGameWinner().playerScore;
//					TournamentWinnerController.game2Loser = selectGameWinner() == player1 ? player2 : player1;
//					TournamentWinnerController.game2LoserScore = TournamentWinnerController.game2Loser.playerScore;
//				} else {
//					TournamentWinnerController.game3Winner = selectGameWinner();
//					TournamentWinnerController.game3WinnerScore = selectGameWinner().playerScore;
//					TournamentWinnerController.game3Loser = selectGameWinner() == player1 ? player2 : player1;
//					TournamentWinnerController.game3LoserScore = TournamentWinnerController.game3Loser.playerScore;
//				}
//				controller.startTournament();
			}
			case "TournamentWinner.fxml" -> {
				TournamentWinnerController controller = loader.getController();
//				controller.showLeaderboard();
			}
		}
		stage = (Stage) confirmCardBtn.getScene().getWindow();
		scene = new Scene(root);
		stage.setScene(scene);
		stage.show();
	}

	/*
	 * Questo metodo imposta i giocatori per la partita
	 *
	 * @param players   ArrayList di giocatori, preso da CreateGameController oppure da un file di salvataggio
	 */
	public static void setPlayers(ArrayList<Player> players) {
		player1 = players.get(0);
		player2 = players.get(1);
	}

	public static void setPlayersFinalTournamentGame(ArrayList<AtomicReference<Player>> players) {
		player1 = players.get(0).get();
		player2 = players.get(1).get();
	}

	/*
	 * Questo metodo inizia la partita, creando un mazzo di carte e distribuendole ai giocatori.
	 */
	public void startNewGame() {
		deck.createDeck();

		if (isTournament) {
			if (code.endsWith("-1")) {
				// MATCH 1
				player1 = CreateGameController.match1.getFirst();
				player2 = CreateGameController.match1.getLast();
			} else if (code.endsWith("-2")) {
				// MATCH 2
//				player1 = CreateGameController.match2.getFirst();
//				player2 = CreateGameController.match2.getLast();
				player1 = TournamentNextGameController.match2Player1;
				player2 = TournamentNextGameController.match2Player2;
			} else {
				// FINAL GAME
//				player1 = CreateGameController.match3.getFirst().get();
//				player2 = CreateGameController.match3.getLast().get();
			}
		}

		// INITIALIZE PLAYER HANDS
		player1Hand = deck.createPlayerDeck();
		player2Hand = deck.createPlayerDeck();
		player1.setHand(player1Hand);
		player2.setHand(player2Hand);

		roundNumber = 1;
		roundLabel.setText("Round: " + roundNumber + " of 6");

		// GAME STARTS WITH PLAYER 1
		currentPlayer = player1;
		selectWildCard(player1);
		selectWildCard(player2);

		GameSave.saveGame();
		playRound();
	}

	/*
	 * Per quando si fanno i cambi di scena, si può riprendere la partita da dove si era fermata
	 */
	public void startGameAfterRound() {
		isNewGame = false;

		// PLAYER HANDS ALREADY INITIALIZED
		player1Hand = player1.getHand();
		player2Hand = player2.getHand();

		roundNumber = RoundWinnerController.getRoundNumber();
		roundLabel.setText("Round: " + roundNumber + " of 6");

		// RESET CHOSEN CARDS FROM PREVIOUS ROUND
		player1.setChosenCard(null);
		player2.setChosenCard(null);

		// NEW ROUND STARTS WITH PLAYER 1
		currentPlayer = player1;
		selectWildCard(player1);
		selectWildCard(player2);

		playRound();
	}

	public void startGameFromSave(String code) {
		isNewGame = false;
		isLoadGame = false;

		// GET DATA FROM .TXT FILE USING SCANNER
		File file = new File(System.getProperty("user.dir") + "/games/" + code + ".txt");
		try {
			Scanner sc = new Scanner(file);
			Game.code = sc.nextLine();
			roundNumber = Integer.parseInt(sc.nextLine());

			Deck initDeck = new Deck();
			ArrayList<Card> initWildsDeck = initDeck.getWildsDeck();
			Random rand = new Random();

			// PLAYER 1
			String p1Name = sc.nextLine();
			int p1Score = Integer.parseInt(sc.nextLine());
			String[] p1WildCardData = sc.nextLine().split(",");
			Card p1WildCard = new Card(p1WildCardData[0], Card.Wild.valueOf(p1WildCardData[1]), Card.Element.valueOf(p1WildCardData[2]), p1WildCardData[3]);
			int p1HandSize = Integer.parseInt(sc.nextLine());
			ArrayList<Card> p1Hand = new ArrayList<>();
			for (int i = 0; i < p1HandSize; i++) {
				String[] cardData = sc.nextLine().split(",");
				Card card = new Card(cardData[0], Card.Element.valueOf(cardData[1]), Double.parseDouble(cardData[2]), cardData[3]);
				p1Hand.add(card);
			}
			player1 = new Player(p1Name);
			player1.playerScore = p1Score;
			player1.setHand(p1Hand);
			player1.setRoundWildCard(p1WildCard);
			p1PreviousWildCard = initWildsDeck.get(rand.nextInt(initWildsDeck.size()));
			player1WildCard = p1WildCard;

			// PLAYER 2
			String p2Name = sc.nextLine();
			int p2Score = Integer.parseInt(sc.nextLine());
			String[] p2WildCardData = sc.nextLine().split(",");
			Card p2WildCard = new Card(p2WildCardData[0], Card.Wild.valueOf(p2WildCardData[1]), Card.Element.valueOf(p2WildCardData[2]), p2WildCardData[3]);
			int p2HandSize = Integer.parseInt(sc.nextLine());
			ArrayList<Card> p2Hand = new ArrayList<>();
			for (int i = 0; i < p2HandSize; i++) {
				String[] cardData = sc.nextLine().split(",");
				Card card = new Card(cardData[0], Card.Element.valueOf(cardData[1]), Double.parseDouble(cardData[2]), cardData[3]);
				p2Hand.add(card);
			}
			player2 = new Player(p2Name);
			player2.playerScore = p2Score;
			player2.setHand(p2Hand);
			player2.setRoundWildCard(p2WildCard);
			p2PreviousWildCard = initWildsDeck.get(rand.nextInt(initWildsDeck.size()));
			player2WildCard = p2WildCard;

			sc.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		player1.setChosenCard(null);
		player2.setChosenCard(null);

		roundLabel.setText("Round: " + roundNumber + " of 6");
		currentPlayer = player1;
		displayPlayerCards(currentPlayer);

		playRound();
	}

	/*
	 * Questo metodo gioca un round della partita, con due turni, uno per ciascun giocatore.
	 * Il metodo seleziona una carta per ciascun giocatore, determina il vincitore, aggiorna i punteggi e il numero del round.
	 */
	public void playRound() {
		confirmCardBtn.setDisable(true);
		if (currentPlayer.isCPU) {
			activateCPU = true;
		}
		playTurn(currentPlayer);

		// IF BOTH PLAYERS HAVE SELECTED A CARD
		if (player1.getChosenCard() != null && player2.getChosenCard() != null) {
			// DETERMINE ROUND WINNER
			p1FinalDmg = finalCardDamage(player1.getChosenCard(), player2.getChosenCard(), player1WildCard);
			p2FinalDmg = finalCardDamage(player2.getChosenCard(), player1.getChosenCard(), player2WildCard);

			if (p1FinalDmg != p2FinalDmg) {
				Player winner = selectRoundWinner();
				winner.playerScore++;
			}

			roundLabel.setText("Round: " + ++roundNumber + " of 6");

			// ROUND LIMIT, END GAME SEQUENCE
			if (roundNumber > 6) {
				if (player1.playerScore == player2.playerScore) {
					// TIEBREAKER ROUND
					Alert alert = new Alert(Alert.AlertType.INFORMATION);
					alert.setTitle("Tiebreaker Round");
					alert.setHeaderText("Punteggio pareggio, inizia il round tiebreaker!");
					alert.show();
					PauseTransition delay = new PauseTransition(Duration.seconds(3));
					delay.setOnFinished(e -> alert.close());
					delay.play();
					roundLabel.setText("Tiebreaker");
					player1.setChosenCard(null);
					player2.setChosenCard(null);
					selectWildCard(player1);
					selectWildCard(player2);
					displayPlayerCards(currentPlayer);
				} else {
					// LOGGING ALL RELEVANT DATA TO "archive" DIRECTORY, AND DELETING THE SAVE FILE FROM "games" DIRECTORY
					GameWinnerController.logGame(player1, player2, selectGameWinner());
					File file = new File(System.getProperty("user.dir") + "/games/" + code + ".txt");
					file.delete();

					// GAME WINNER SCENE
					activateCPU = false;
					if (!isTournament) {
						try {
							switchToScene("GameWinner.fxml");
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					} else {
						// TOURNAMENT MODE
						// IF FINAL GAME, DISPLAY TOURNAMENT WINNER
						if (code.endsWith("-3")) {
							try {
								switchToScene("TournamentWinner.fxml");
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
						} else {
							// IF NOT FINAL GAME, DISPLAY NEXT GAME
							try {
								switchToScene("TournamentNextGame.fxml");
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
						}
					}
					// THE GAME ENDS HERE
					resetGameCondition();
				}
			} else {
				// ROUND WINNER SCENE
				try {
					switchToScene("RoundWinner.fxml");
				} catch (IOException e) {
					throw new RuntimeException(e);
				}

				// PREPARING FOR THE NEXT ROUND
				player1.setChosenCard(null);
				player2.setChosenCard(null);

				// SELECT NEW WILD CARDS FOR THE NEXT ROUND FOR BOTH PLAYERS
				selectWildCard(player1);
				selectWildCard(player2);

				// DISPLAY CARDS FOR THE NEXT ROUND, STARTING WITH PLAYER 1
				displayPlayerCards(currentPlayer);
			}
		}
	}

	/*
	 * Questo salva la carta selezionata dal giocatore in un round
	 */
	public void playTurn(Player player) {
		playerLabel.setText("Scegli una carta, " + player.getName());
		playerTurnLabel.setText("TURNO: " + player.getName());
		displayPlayerCards(player);
		AtomicReference<Card> selectedCard = new AtomicReference<>();
		if (player.isCPU) {
			// CPU SELECTS CARD AFTER DELAY
			PauseTransition delay = new PauseTransition(Duration.seconds(0.25));
			delay.setOnFinished(e -> {
				if (activateCPU) {
					selectedCard.set(player.returnBestCardCPU());
					selectCardCPU(selectedCard.get());

					// CPU CLICKS CONFIRM BUTTON AFTER DELAY
					PauseTransition delayConfirm = new PauseTransition(Duration.seconds(0.25));
					delayConfirm.setOnFinished(event -> {
						player.setChosenCard(selectedCard.get());
						confirmCardBtn.fire();
					});
					delayConfirm.play();
				}
			});
			delay.play();
		} else {
			selectedCard.set(player.getChosenCard());
			player.setChosenCard(selectedCard.get());
		}
	}

	/*
	 * Connesso al bottone confirmCardBtn, questo metodo conferma la carta selezionata dal giocatore
	 * e termina il round una volta che entrambi hanno giocato, confrontando le carte selezionate dai giocatori
	 */
	@FXML
	void onConfirmCard() {
		currentPlayer.removeUsedCard();
		currentPlayer = (currentPlayer == player1) ? player2 : player1;

		// DEFAULT CARD IMAGE START FOR NEXT PLAYER
		InputStream is = getClass().getResourceAsStream("/cards/default_card.jpeg");
		Image defaultCard = new Image(is);
		playerCardChoiceImage.setImage(defaultCard);
		damageCardChoiceLabel.setText("DANNO: ");

		playRound();
	}

	/*
	 * Questo metodo fa il display delle carte disponibili per il giocatore e il suo wild card
	 */
	void displayPlayerCards(Player player) {
		ArrayList<Card> cards = player.getHand();
		ImageView[] cardViews = {
				playerCard1, playerCard2, playerCard3,
				playerCard4, playerCard5, playerCard6,
				playerCard7, playerCard8, playerCard9
		};

		// CLEAR CARD VIEWS
		for (ImageView cardView : cardViews) {
			cardView.setImage(null);
		}

		// SORT HAND DISPLAY BY ENUM (0 = FIRE, 1 = WATER, 2 = FOREST)
		cards.sort(Comparator.comparing(card -> card.getElement().ordinal()));

		cardMap.clear();
		for (int i = 0; i < cards.size(); i++) {
			Image image = cards.get(i).getArt();
			cardViews[i].setImage(image);
			cardMap.put(cardViews[i], cards.get(i));
		}

		Card wildCard = player.getRoundWildCard();
		// DISPLAY WILD CARD EFFECTS
		String wildEffects =
				wildCard.getWild().toString() + " DANNO" +
						"\n--> " + (wildCard.getWild().toString().equals("INDEBOLIMENTO") ? "0.8x" : "1.2x" + " CARTE " + wildCard.getElement().toString());
		wildEffectsLabel.setText(wildEffects);
		if (wildCard.getWild() == Card.Wild.POTENZIAMENTO) {
			wildEffectsLabel.setStyle("-fx-text-fill: lime");
		} else {
			wildEffectsLabel.setStyle("-fx-text-fill: red");
		}

		// DISPLAY WILD CARD IMAGE
		Image image = wildCard.getArt();
		wildCardImage.setImage(image);
	}

	/*
	 * Viene chiamato all'inizio di ogni round, si applica una delle carte Wild (Indebolimento, Potenziamento)
	 */
	public void selectWildCard(Player player) {
		// TO PREVENT THE SAME WILD CARD FROM BEING SELECTED TWICE IN A ROW
		if (player == player1) {
			p1PreviousWildCard = player1WildCard;
		} else {
			p2PreviousWildCard = player2WildCard;
		}

		// RANDOMLY SELECT A WILD CARD THAT IS NOT THE SAME AS THE PREVIOUS ROUND
		Random rand = new Random();
		Card wildCard;
		do {
			wildCard = wildsDeck.get(rand.nextInt(wildsDeck.size()));
		} while (wildCard == p1PreviousWildCard && player == player1 || wildCard == p2PreviousWildCard && player == player2);

		player.setRoundWildCard(wildCard);

		if (player == player1) {
			player1WildCard = wildCard;
		} else {
			player2WildCard = wildCard;
		}
	}

	/*
	 * Questo metodo permette al giocatore di selezionare una carta dal proprio mazzo
	 */
	@FXML
	void selectCard(MouseEvent event) {
		confirmCardBtn.setDisable(false);

		ImageView clickedCard = (ImageView) event.getSource();
		selectedCard = cardMap.get(clickedCard);

		displayCardChoice(clickedCard, selectedCard != null, selectedCard);
	}

	/*
	 * Questo metodo permette al giocatore CPU di selezionare una carta dal proprio mazzo
	 * È identico a selectCard(), ma è chiamato da returnBestCardCPU() per selezionare la carta CPU dopo un breve delay.
	 */
	public void selectCardCPU(Card card) {
		confirmCardBtn.setDisable(false);

		// Find the ImageView associated with the card
		ImageView clickedCard = null;
		for (Map.Entry<ImageView, Card> entry : cardMap.entrySet()) {
			if (entry.getValue().equals(card)) {
				clickedCard = entry.getKey();
				break;
			}
		}

		displayCardChoice(clickedCard, clickedCard != null, card);
	}

	private void displayCardChoice(ImageView clickedCard, boolean validCard, Card selectedCard) {
		if (validCard) {
			// DISPLAY SELECTED CARD IMAGE IF NOT NULL
			Image cardImage = clickedCard.getImage();
			playerCardChoiceImage.setImage(cardImage);
		} else {
			// DISPLAY DEFAULT CARD IMAGE IF NULL
			InputStream is = getClass().getResourceAsStream("/cards/default_card.jpeg");
			Image defaultCard = new Image(is);
			playerCardChoiceImage.setImage(defaultCard);
		}

		Card playerWildCard;
		if (currentPlayer == player1) {
			playerWildCard = player1WildCard;
		} else {
			playerWildCard = player2WildCard;
		}

		if (selectedCard != null) {
			// CALCULATE & DISPLAY DAMAGE OF SELECTED CARD WITH WILD CARD (ENEMY ELEMENT UNKNOWN)
			confirmCardBtn.setDisable(false);
			double mult = wildCardMultiplier(selectedCard, playerWildCard);
			double cardDamage = selectedCard.getDamage();
			damageCardChoiceLabel.setText("DANNO: " + Math.round((cardDamage * mult) * 10.0) / 10.0);
			currentPlayer.setChosenCard(selectedCard);
		} else {
			// DISABLE CONFIRM BUTTON IF NULL CARD VIEW CLICKED
			confirmCardBtn.setDisable(true);
			damageCardChoiceLabel.setText("DANNO: ");
		}
	}

	/*
	 * Calcola il moltiplicatore della carta selezionata, in base alla carta Wild del round
	 * È diverso da finalCardDamage() perché considera solo la carta selezionata e la carta Wild del round
	 * e non la carta dell'avversario. Inoltre, fornisce solo il moltiplicatore e non il danno finale.
	 */
	static double wildCardMultiplier(Card card, Card wildCard) {
		double mult = 1.0;

		// FOR CODE READABILITY
		Card.Element cardElem = card.getElement();
		Card.Element wildElem = wildCard.getElement();

		if (wildCard.getWild() == Card.Wild.POTENZIAMENTO) {
			if (cardElem == FIRE && wildElem == FIRE) {
				mult = 1.2;
			} else if (cardElem == WATER && wildElem == WATER) {
				mult = 1.2;
			} else if (cardElem == FOREST && wildElem == FOREST) {
				mult = 1.2;
			}
		} else {
			if (cardElem == FIRE && wildElem == FIRE) {
				mult = 0.8;
			} else if (cardElem == WATER && wildElem == WATER) {
				mult = 0.8;
			} else if (cardElem == FOREST && wildElem == FOREST) {
				mult = 0.8;
			}
		}

		// WILD MULTIPLIER FOR ROUND WINNER SCENE
		if (player1.getChosenCard() == card) {
			RoundWinnerController.p1WildMult = mult;
		} else {
			RoundWinnerController.p2WildMult = mult;
		}

		return mult;
	}

	/*
	 * Questo metodo calcola il danno finale di una carta, in base al danno base e ad eventuali modificatori:
	 * - Indebolimento: 0.8x danno
	 * - Potenziamento: 1.2x danno
	 *
	 * - Fuoco attacca Terra: 1.2x danno (0.8x se vice-versa)
	 * - Terra attacca Acqua: 1.2x danno (0.8x se vice-versa)
	 * - Acqua attacca Fuoco: 1.2x danno (0.8x se vice-versa)
	 */
	double finalCardDamage(Card playerCard, Card opponentCard, Card roundWildCard) {
		double dmg = playerCard.getDamage();
		double mult = 1.0;

		// FOR CODE READABILITY
		final Card.Element playerElem = playerCard.getElement();
		final Card.Element opponentElem = opponentCard.getElement();

		// ELEMENTAL ADVANTAGE CASE
		if (playerElem == FIRE && opponentElem == FOREST) {
			mult = 1.2;
		} else if (playerElem == FOREST && opponentElem == WATER) {
			mult = 1.2;
		} else if (playerElem == WATER && opponentElem == FIRE) {
			mult = 1.2;
		}

		// ELEMENTAL DISADVANTAGE CASE
		if (playerElem == FIRE && opponentElem == WATER) {
			mult = 0.8;
		} else if (playerElem == WATER && opponentElem == FOREST) {
			mult = 0.8;
		} else if (playerElem == FOREST && opponentElem == FIRE) {
			mult = 0.8;
		}

		// ELEMENTAL MULTIPLIER FOR ROUND WINNER SCENE
		if (player1.getChosenCard() == playerCard) {
			RoundWinnerController.p1ElemMult = mult;
		} else {
			RoundWinnerController.p2ElemMult = mult;
		}

		// WILD CARD MULTIPLIER APPLIED TO GET FINAL MULTIPLIER
		mult *= wildCardMultiplier(playerCard, roundWildCard);
		return Math.round((dmg * mult) * 10.0) / 10.0;
	}

	public static Player selectRoundWinner() {
		return p1FinalDmg > p2FinalDmg ? player1 : player2;
	}

	public static Player selectGameWinner() {
		return player1.playerScore > player2.playerScore ? player1 : player2;
	}

	@FXML
	void showScoreboard() {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Scoreboard");
		alert.setHeaderText("Punteggi attuali");
		alert.setContentText(player1.getName() + ":\t" + player1.playerScore + " punti\n" + player2.getName() + ":\t" + player2.playerScore + " punti");
		alert.show();
		PauseTransition delay = new PauseTransition(Duration.seconds(5));
		delay.setOnFinished(e -> alert.close());
		delay.play();
	}

	@FXML
	void saveGame(ActionEvent event) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Salvataggio partita");
		alert.setHeaderText("Partita salvata con successo");
		alert.show();
		PauseTransition delay = new PauseTransition(Duration.seconds(3));
		delay.setOnFinished(e -> alert.close());
		delay.play();
		GameSave.saveGame();
	}

	@FXML
	void quitAndSaveGame(ActionEvent event) {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Conferma uscita");
		alert.setHeaderText("Sei sicuro di voler uscire?");
		alert.setContentText("Il gioco verrà salvato e chiuso.");
		alert.showAndWait();
		if (alert.getResult().getText().equals("OK")) {
			GameSave.saveGame();
			System.exit(0);
		} else {
			alert.close();
		}
	}

	/*
	 * Questo metodo fa ripartire la partita da capo, utilizzando gli stessi giocatori
	 */
	@FXML
	void restartGame(ActionEvent event) {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Restart Game");
		alert.setHeaderText("Sei sicuro di voler ricominciare la partita?");
		alert.setContentText("Il round e punteggi verranno azzerati");
		alert.showAndWait();

		if (alert.getResult().getText().equals("OK")) {
			// Salva i nomi dei giocatori pre-resetGameCondition
			String p1Name = player1.getName(), p2Name = player2.getName();
			resetGameCondition();
			// Riavvia la partita con gli stessi giocatori
			CreateGameController.players = new ArrayList<>();
			CreateGameController.players.add(new Player(p1Name));
			CreateGameController.players.add(new Player(p2Name));
			CreateGameController.activeCodes.add(CreateGameController.code);
			setPlayers(CreateGameController.players);
			roundNumber = 1;
			InputStream is = getClass().getResourceAsStream("/cards/default_card.jpeg");
			Image defaultCard = new Image(is);
			playerCardChoiceImage.setImage(defaultCard);
			damageCardChoiceLabel.setText("DANNO: ");
			startNewGame();
		} else {
			alert.close();
		}
	}

	/*
	 * Questo metodo resetta le variabili della partita, in preparazione per una nuova partita
	 */
	void resetGameCondition() {
		CreateGameController.players.clear();
		deck.clearAllDecks();
		player1 = null;
		player2 = null;
		player1Hand = null;
		player2Hand = null;
		player1WildCard = null;
		player2WildCard = null;
		p1PreviousWildCard = null;
		p2PreviousWildCard = null;
		selectedCard = null;
		roundLabel.setText("Round: " + roundNumber + " of 6");
		CreateGameController.activeCodes.remove(CreateGameController.code);
	}
}