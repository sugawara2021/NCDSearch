package sarf.lexer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.Reader;


public class CodeTokenizer implements TokenReader {

	public enum CommentRemoval { ENABLED, DISABLED };
	List<Token> tokens;
	private int index = -1;
	private CommentRemoval removeComments = CommentRemoval.DISABLED;
	
	public CodeTokenizer(Reader r, CommentRemoval removeComments) {
		// r からファイルの中身をロードする
		this.removeComments = removeComments;
		tokens = processFile(r);
	}
	
    @Override
    public boolean next() {
    	// TODO Auto-generated method stub
    	if(tokens != null) {
    		index++;
    		if(index < tokens.size()) {
    			return true;
    		}
    	}
    	return false;
    }
    
    @Override
    public String getToken() {
    	if(index >= 0 && index < tokens.size()) {
    		Token specificToken = tokens.get(index);
    		return specificToken.value;
    	}
    	
    	return null;
    }
    
    @Override
    public String getNormalizedToken() {
    	// TODO Auto-generated method stub
    	return getToken();
    }
    
    @Override
    public int getLine() {
    	// TODO Auto-generated method stub
    	 if(index >= 0 && index < tokens.size()) {
    		 Token specificToken = tokens.get(index);
    		 return specificToken.line;
    	 }
    	return 0;
    }
    
    @Override
    public FileType getFileType() {
    	// TODO Auto-generated method stub
    	return FileType.NEUTRAL;
    }
    
    @Override
    public int getCharPositionInLine() {
    	// TODO Auto-generated method stub
    	 if(index >= 0 && index < tokens.size()) {
    		 Token specificToken = tokens.get(index);
    		 return specificToken.position;
    	 }
    	return 0;
    }
    
    private List<Token> processFile(Reader r) {
        List<Token> tokens = new ArrayList<>();
        try (Scanner scanner = new Scanner(r)) {
            int lineNumber = 0;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                lineNumber++;
                if (removeComments == CommentRemoval.ENABLED) {
                    line = removeComments(line);
                }
                tokens.addAll(tokenizeCode(line, lineNumber));
            }
        } catch (Exception e) {
            //System.out.println("An error occurred while processing the file: " + file.getPath());
            e.printStackTrace();
        }
        return tokens;
    }

    private static String removeComments(String code) {
        try {
            // Remove /* */ comments, including multiline comments
            String noBlockComments = code.replaceAll("(?s)/\\*.*?\\*/", "");
            // Remove // comments
            return noBlockComments.replaceAll("//.*", "");
        } catch (Exception e) {
            System.out.println("An error occurred while removing comments.");
            e.printStackTrace();
            return code; // Return original code if an error occurs
        }
    }

    private static List<Token> tokenizeCode(String code, int lineNumber) {
        List<Token> tokens = new ArrayList<>();
        try {
            // Regex to match words, numbers, underscores or single non-alphanumeric characters
            Pattern pattern = Pattern.compile("[a-zA-Z0-9_]+|[^\\s\\w]");
            Matcher matcher = pattern.matcher(code);

            while (matcher.find()) {
                tokens.add(new Token(matcher.group(), lineNumber, matcher.start() + 1));
            }
        } catch (Exception e) {
            System.out.println("An error occurred while tokenizing the code.");
            e.printStackTrace();
        }
        return tokens;
    }

    
    static class Token {
        String value;
        int line;
        int position;

        Token(String value, int line, int position) {
            this.value = value;
            this.line = line;
            this.position = position;
        }
    }

}

