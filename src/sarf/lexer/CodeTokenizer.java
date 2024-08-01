package sarf.lexer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeTokenizer implements TokenReader {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java CodeTokenizer <filename|directory> [-hideTokens]");
            return;
        }

        String filePath = args[0];
        boolean hideTokens = false;

        for (int i = 1; i < args.length; i++) {
            if (args[i].equals("-hideTokens")) {
                hideTokens = true;
            }
        }

        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("File or directory not found: " + filePath);
            return;
        }

        List<String> tokens = new ArrayList<>();
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                if (f.isFile()) {
                    tokens.addAll(processFile(f));
                }
            }
        } else {
            tokens = processFile(file);
        }

        if (!hideTokens) {
            for (String token : tokens) {
                System.out.println(token);
            }
        }
    }
    
    @Override
    public boolean next() {
    	// TODO Auto-generated method stub
    	return false;
    }
    
    @Override
    public String getToken() {
    	// TODO Auto-generated method stub
    	return null;
    }
    
    @Override
    public String getNormalizedToken() {
    	// TODO Auto-generated method stub
    	return null;
    }
    
    @Override
    public int getLine() {
    	// TODO Auto-generated method stub
    	return 0;
    }
    
    @Override
    public FileType getFileType() {
    	// TODO Auto-generated method stub
    	return null;
    }
    
    @Override
    public int getCharPositionInLine() {
    	// TODO Auto-generated method stub
    	return 0;
    }
    

    private static List<String> processFile(File file) {
        List<String> tokens = new ArrayList<>();
        try (Scanner scanner = new Scanner(file)) {
            scanner.useDelimiter("\\Z"); // Read entire file as a single string
            String code = scanner.next();
            String sanitizedCode = removeComments(code);
            tokens = tokenizeCode(sanitizedCode);
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + file.getPath());
        } catch (Exception e) {
            System.out.println("An error occurred while processing the file: " + file.getPath());
            e.printStackTrace();
        }
        return tokens;
    }

    private static String removeComments(String code) {
        try {
            // Remove /* */ comments
            String noBlockComments = code.replaceAll("(?s)/\\*.*?\\*/", "");
            // Remove // comments
            return noBlockComments.replaceAll("//.*", "");
        } catch (Exception e) {
            System.out.println("An error occurred while removing comments.");
            e.printStackTrace();
            return code; // Return original code if an error occurs
        }
    }

    private static List<String> tokenizeCode(String code) {
        List<String> tokens = new ArrayList<>();
        try {
            // Regex to match words, numbers, underscores or single non-alphanumeric characters
            Pattern pattern = Pattern.compile("[a-zA-Z0-9_]+|[^\\s\\w]");
            Matcher matcher = pattern.matcher(code);

            while (matcher.find()) {
                tokens.add(matcher.group());
            }
        } catch (Exception e) {
            System.out.println("An error occurred while tokenizing the code.");
            e.printStackTrace();
        }
        return tokens;
    }
}
