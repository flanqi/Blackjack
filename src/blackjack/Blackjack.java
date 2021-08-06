package blackjack;
import java.util.*;


public class Blackjack implements BlackjackEngine {
	private Random randomGenerator;
	
	private int numberOfDecks;
	private ArrayList<Card> deckOfCards;
	private ArrayList<Card> dealerDecks;
	private ArrayList<Card> playerDecks;
	
	private int playerAccount=200;
	private int bet = 5; // initial bet amount is 5
	
	private int status;
	
	
	/**
	 * Constructor you must provide.  Initializes the player's account 
	 * to 200 and the initial bet to 5.  Feel free to initialize any other
	 * fields. Keep in mind that the constructor does not define the 
	 * deck(s) of cards.
	 * @param randomGenerator
	 * @param numberOfDecks
	 */
	
	public Blackjack(Random randomGenerator, int numberOfDecks) {
	    this.randomGenerator = randomGenerator;
		this.numberOfDecks = numberOfDecks;
		this.deckOfCards = new ArrayList<>();
		this.dealerDecks = new ArrayList<>();
		this.playerDecks = new ArrayList<>();
	}
	
	public int getNumberOfDecks() {
		return numberOfDecks;
	}
	
	public void createAndShuffleGameDeck() {
		// creates numberOfDecks decks
		for(int i=0;i<numberOfDecks;i++) {
			createGameDeck(this);
		}
		
		
		// shuffle
		Collections.shuffle(deckOfCards, randomGenerator);
	}
	
	// creates one deck
	private static void createGameDeck(Blackjack blackjack) {
		
		for (CardSuit suit : CardSuit.values()) {
			for (CardValue value : CardValue.values()) {
				blackjack.deckOfCards.add(new Card(value, suit));
			}
		}
				
	}
	
	public Card[] getGameDeck() {
		return getCardArray(this.deckOfCards);
	}
	
	// turn the ArrayList to a Card array
	private static Card[] getCardArray(ArrayList<Card> list) {
		Card[] cards = new Card[list.size()];
		for(int i=0;i<list.size();i++) {
			cards[i] = list.get(i);
		}
		return cards;
	}
	
	// clears all current decks
	private void refresh() {
		this.deckOfCards = new ArrayList<>();
		this.dealerDecks = new ArrayList<>();
		this.playerDecks = new ArrayList<>();
	}
	
	public void deal() {	
		
		this.refresh(); // clears decks from the last round
		this.createAndShuffleGameDeck(); // creates numberOfDecks decks
		
		playerDecks.add(retrieve()); 
		Card dealerCard1 = retrieve();
		dealerCard1.setFaceDown(); // set dealer's first card face down
		dealerDecks.add(dealerCard1); 
		playerDecks.add(retrieve()); 
		dealerDecks.add(retrieve()); 
		
		playerAccount -= 5;
		
		status = GAME_IN_PROGRESS;
		
	}
	
	// returns and removes the first card in deckOfCards
	private Card retrieve() {
		Card card = deckOfCards.get(0);
		deckOfCards.remove(0);
		return card;
	}
	
	public Card[] getDealerCards() {
		return getCardArray(this.dealerDecks);
	}

	public int[] getDealerCardsTotal() {
		return this.getCardsTotal(0);
	}
	
	// getCardsTotal for dealer if n==0, for player otherwise
	private int[] getCardsTotal(int n) {
		Card[] cards = (n == 0) ? this.getDealerCards() : this.getPlayerCards();
		
		ArrayList<Integer> list = new ArrayList<>(); 
		int sumWithoutAce=0; // sum of values of cards which are not aces
		for(Card card : cards) {
			if(card.getValue()!=CardValue.Ace) {
				sumWithoutAce += card.getValue().getIntValue();
			}
		}
		
		int numberOfAce = numAce(cards); // number of aces in dealer's cards
		
		// sum of aces (if there are some) in a deck can only be 1, 11, 12, 13 for the value
		// of a deck to be less than 21
		list.add(numberOfAce+sumWithoutAce); // only adds sumWithoutAce if there's no Ace
		if(numberOfAce>0) {	
			list.add(10+numberOfAce+sumWithoutAce);
		}
		
		Collections.sort(list);
		
		if(list.get(0)>21) {
			return null;
		} else if(list.size()==1||list.get(1)>21){
			int[] list1 = {list.get(0)};
			return list1 ;
		} else {
			int[] list2 = {list.get(0),list.get(1)};
			return list2;
		}
	}
	
	// gets the number of aces in a card array
	private static int numAce(Card[] cards) {
		int n=0;
		for (Card card : cards) {
			if(card.getValue() == CardValue.Ace) {
				n++;
			}
		}
		return n;
	}
	
	public int getDealerCardsEvaluation() {
		if(this.getDealerCardsTotal() == null) {
			return BUST;
		} else if(numAce(getDealerCards())>0 && hasTenJQK(this.getDealerCards())) {
			return BLACKJACK;
		} else if(has21(this.getDealerCardsTotal())) {
			return HAS_21;
		} else {
			return LESS_THAN_21;
		}
	}
	
	// returns true if a Card array has a Ten, Jack, Queen or King
	private static boolean hasTenJQK(Card[] cards) {
		for(Card card : cards) {
			boolean isTen = card.getValue() == CardValue.Ten;
			boolean isJ = card.getValue() == CardValue.Jack;
			boolean isQ = card.getValue() == CardValue.Queen;
			boolean isK = card.getValue() == CardValue.King;
			if(isTen||isJ||isQ||isK) {
				return true;
			}
		}
		return false;
	}
	
	// returns true if an int list has 21
	private static boolean has21(int[] list) {
		for(int i : list) {
			if(i==21) {
				return true;
			}
		}
		return false;
	}
	
	public Card[] getPlayerCards() {
		return getCardArray(this.playerDecks);
	}
	
	public int[] getPlayerCardsTotal() {
		return this.getCardsTotal(1);
	}
	
	public int getPlayerCardsEvaluation() {
		if(this.getPlayerCardsTotal() == null) {
			return BUST;
		} else if(numAce(this.getPlayerCards())>0 && hasTenJQK(this.getPlayerCards())) {
			return BLACKJACK;
		} else if(has21(this.getPlayerCardsTotal())) {
			return HAS_21;
		} else {
			return LESS_THAN_21;
		}
	}
	
	public void playerHit() {
		this.playerDecks.add(retrieve());
		int a = this.getPlayerCardsEvaluation();
		if(a==BUST) {
			status = DEALER_WON;
		}else {
			status = GAME_IN_PROGRESS;
		}
	}
	
	public void playerStand() {
		this.dealerDecks.get(0).setFaceUp();
		int[] dealer = this.getDealerCardsTotal();
		int[] player = this.getPlayerCardsTotal(); // already sorted
		while(this.getDealerCardsEvaluation()!=BUST && dealer[dealer.length-1]<16) {
			this.dealerDecks.add(retrieve());
			dealer = this.getDealerCardsTotal();
		}
		if(this.getDealerCardsEvaluation()==BUST) {
			status = PLAYER_WON;
			this.playerAccount+= 2*bet;
		} else if(dealer[dealer.length-1]>player[player.length-1]) {
			status = DEALER_WON;
		} else if(dealer[dealer.length-1]==player[player.length-1]) {
			status = DRAW;
			this.playerAccount+= bet;
		} else {
			status = PLAYER_WON;
			this.playerAccount+= 2*bet;
		}
	}
	
	public int getGameStatus() {
		return status;
	}
		
	public void setBetAmount(int amount) {
		bet = amount;
	}
	
	public int getBetAmount() {
		return bet;
	}
	
	public void setAccountAmount(int amount) {	
		playerAccount = amount;
	}
	
	public int getAccountAmount() {
		return playerAccount;
	}
	
	/* Feel Free to add any private methods you might need */
}