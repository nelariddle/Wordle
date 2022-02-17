import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.sound.sampled.SourceDataLine;

public class Wordle {
  // for each guess
  // 1 = gray = not in word OR first instance of letter was already revealed
  // 1 = yellow = in word, wrong spot
  // 2 = green = in word, right spot

  // for each letter
  // m = might appear
  // y = appears
  // n = doesn't appear
  public static void main(String[] args) throws Exception {
    ArrayList<String> dictionary = new ArrayList<>();
    try {
      File myObj = new File("dict.txt");
      Scanner myReader = new Scanner(myObj);
      while (myReader.hasNextLine()) {
        String data = myReader.nextLine();
        if (data.length() == 5)
          dictionary.add(data.toLowerCase());
      }
      myReader.close();
    } catch (FileNotFoundException e) {
      System.out.println("An error occurred.");
      e.printStackTrace();
    }
    runProgram(dictionary);
  }

  public static int clueFromGuess(String guess, String word) {
    int clue = -1;
    for (char c : guess.toCharArray()) {
    }

    return clue;
  }

  public static void eliminateWords(ArrayList<String> dict, HashMap<Character, String> letterClues) {
    dict.removeIf(word -> !wordStillPossible(word, letterClues));
  }

  public static boolean wordStillPossible(String dictWord, HashMap<Character, String> letterClues) {
    for (Map.Entry<Character, String> entry : letterClues.entrySet()) {
      char letter = entry.getKey();
      String clue = entry.getValue();
      if (!matchesLetterPattern(dictWord, clue, letter)) {
        return false;
      }
    }
    return true;
  }

  public static boolean matchesLetterPattern(String dictWord, String letterPattern, char letter) {
    int i = 0;
    boolean containsLetter = false;
    for (char c : dictWord.toCharArray()) {
      if (letter == c && letterPattern.charAt(i) == 'n') {
        return false;
      }
      if (letter != c && letterPattern.charAt(i) == 'y') {
        return false;
      }
      if (c == letter) {
        containsLetter = true;
      }
      i++;
    }
    if (!containsLetter) {
      return letterPattern.equals("nnnnn");
    }
    return true;
  }

  // returns digit at position pos where the far left is 0
  public static int digitAtPos(int num, int pos) {
    return (int) (num / (Math.pow(10, (int) (Math.log10(num) + 1) - pos - 1))) % 10;
  }

  public static String uniqueLetters(String word) {
    String unique = "";
    for (char c : word.toCharArray()) {
      if (unique.indexOf(c) == -1) {
        unique += c;
      }
    }
    return unique;
  }

  public static HashMap<Character, String> mergeClues(HashMap<Character, String> originalClues,
      HashMap<Character, String> newClues) {
    for (Map.Entry<Character, String> entry : newClues.entrySet()) {
      char letter = entry.getKey();
      if (originalClues.keySet().contains(letter)) {
        originalClues.replace(letter, mergeClue(originalClues.get(letter), entry.getValue()));
      } else {
        originalClues.put(letter, entry.getValue());
      }
    }
    return originalClues;
  }

  public static String mergeClue(String originalClue, String newClue) {
    String mergedClue = "";
    for (int i = 0; i < 5; i++) {
      if (newClue.charAt(i) == 'm') {
        mergedClue += originalClue.charAt(i);
      } else {
        mergedClue += newClue.charAt(i);
      }
    }

    return mergedClue;
  }

  public static HashMap<Character, String> generateLetterClues(String guess, int clue) {
    String letters = uniqueLetters(guess);
    HashMap<Character, String> clues = new HashMap<>();
    for (char letter : letters.toCharArray()) {
      String letterClue = "mmmmm";
      boolean letterAppearsTwice = letterAppearsTwice(guess, letter);
      for (int i = 0; i < 5; i++) {
        if (guess.charAt(i) == letter) {
          if (digitAtPos(clue, i) == 3) {
            letterClue = letterClue.substring(0, i) + "y" + letterClue.substring(i + 1);
          } else if (digitAtPos(clue, i) == 2) {
            letterClue = letterClue.substring(0, i) + "n" + letterClue.substring(i + 1);
          } else if (!letterAppearsTwice) {
            letterClue = "nnnnn";
          }
        }
      }
      clues.put(letter, letterClue);
    }
    return clues;
  }

  public static boolean letterAppearsTwice(String word, char letter) {
    String unique = "";
    for (char c : word.toCharArray()) {
      if (unique.indexOf(c) == -1) {
        unique += c;
      } else {
        return true;
      }
    }
    return false;
  }

  public static String generateNewGuess(ArrayList<String> dict) {
    HashMap<Character, Integer> prevalences = new HashMap<>();
    for (String word : dict) {
      for (char c : word.toCharArray()) {
        if (prevalences.containsKey(c)) {
          prevalences.replace(c, prevalences.get(c) + 1);
        } else {
          prevalences.put(c, 1);
        }
      }
    }
    int max = 0;
    String best = "";
    for (String word : dict) {
      int current = 0;
      boolean skip = false;
      if (uniqueLetters(word).length() != 5) {
        skip = true;
      }
      for (char c : word.toCharArray()) {
        current += prevalences.get(c);
      }
      if (current > max && !skip) {
        max = current;
        best = word;
      }
    }

    // prevalences.forEach((k, v) -> System.out.println("Key = "
    // + k + ", Value = " + v));

    // return dict.get((int) (Math.random() * dict.size()));
    return best.length() == 0 ? dict.get(0) : best;
  }

  public static void runProgram(ArrayList<String> dict) {
    HashMap<Character, String> letterClues = new HashMap<>();
    String guess = "";
    int clue = -1;

    Scanner s = new Scanner(System.in);
    while (clue != 33333) {
      System.out.println(generateNewGuess(dict));

      // System.out.println("what was your guess?");
      // guess = s.nextLine();
      System.out.println("YOU SHOULD GUESS " + generateNewGuess(dict));

      guess = generateNewGuess(dict);
      System.out.println("what was your clue? (1=gray,2=yellow,3=green)");
      clue = Integer.valueOf(s.nextLine());

      letterClues = mergeClues(letterClues, generateLetterClues(guess, clue));
      eliminateWords(dict, letterClues);
    }
    System.out.println("nice");
  }
}
