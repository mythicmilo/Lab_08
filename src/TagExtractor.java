import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static java.nio.file.StandardOpenOption.CREATE;

public class TagExtractor extends JFrame
{
    //Map <KeyType, ValType> mapName = new Hash/TreeMap<>();
    //mapName.put(key, value) adds key and value (replaces existing value if key is same)
    //mapName.get(key) gets value associated with key, not key itself
    //Map <String, Integer> freq = new TreeMap<>();
    //Set <String> setName = new Hash/TreeSet<>();
    JPanel mainPnl, fileNamePnl, displayPnl, ctrlPnl;
    JButton extractBtn, quitBtn;
    JLabel fileNameLbl;
    JTextField fileNameTF;
    JTextArea displayTA;
    JScrollPane scroller;
    ArrayList<String> allWords = new ArrayList<>();
    Set<String> noiseWords = new TreeSet<>();

    public TagExtractor()
    {
        mainPnl = new JPanel();
        mainPnl.setLayout(new BorderLayout());
        add(mainPnl);

        createDisplayPanel();
        createControlPanel();

        setTitle("Tag Extractor");
        setSize(650, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    public void createDisplayPanel()
    {
        fileNamePnl = new JPanel();
        displayPnl = new JPanel();
        displayPnl.setLayout(new BorderLayout());
        fileNameLbl = new JLabel("File Name:");
        fileNameTF = new JTextField();
        fileNameTF.setEditable(false);
        displayTA = new JTextArea(15, 50);
        displayTA.setMargin(new Insets(10, 10, 10, 10));
        displayTA.setEditable(false);
        scroller = new JScrollPane(displayTA);

        fileNamePnl.add(fileNameLbl);
        fileNamePnl.add(fileNameTF);
        displayPnl.add(fileNamePnl, BorderLayout.NORTH);
        displayPnl.add(scroller, BorderLayout.CENTER);

        mainPnl.add(displayPnl, BorderLayout.CENTER);
    }

    public void createControlPanel()
    {
        ctrlPnl = new JPanel();
        extractBtn = new JButton("Extract Tags from File");
        extractBtn.addActionListener((ActionEvent e) -> {
            resetExtractor();
            extractFrom();
            noiseFrom();
            excludeNoise();
            displayTagFreq();
        });
        quitBtn = new JButton("Quit");
        quitBtn.addActionListener((ActionEvent e) -> {
            int response = JOptionPane.showConfirmDialog(null, "Are you sure you want to quit?", "Confirm Quit", JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.YES_OPTION)
            {
                System.exit(0);
            }
        });

        ctrlPnl.add(extractBtn);
        ctrlPnl.add(quitBtn);
        mainPnl.add(ctrlPnl, BorderLayout.SOUTH);
    }

    public void resetExtractor()
    {
        displayTA.setText("");
        allWords.clear();
        noiseWords.clear();
    }

    public void extractFrom()
    {
        JFileChooser chooser = new JFileChooser();
        File selectedFile;
        String rec = "";
        StringTokenizer tokenizer;
        String word = "";
        JOptionPane.showMessageDialog(null, "Please choose a file to extract tags from", "Choose Tags Source", JOptionPane.INFORMATION_MESSAGE);
        try
        {
            File workingDirectory = new File(System.getProperty("user.dir"));

            chooser.setCurrentDirectory(workingDirectory);

            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
            {
                selectedFile = chooser.getSelectedFile();
                Path file = selectedFile.toPath();
                Path selectedFileName = file.getFileName();
                String fileName = selectedFileName.toString();
                fileNameTF.setText(fileName);
                fileNamePnl.revalidate();
                fileNamePnl.repaint();

                InputStream in =
                        new BufferedInputStream(Files.newInputStream(file, CREATE));
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(in));

                while (reader.ready())
                {
                    rec = reader.readLine();
                    tokenizer = new StringTokenizer(rec);
                    while (tokenizer.hasMoreTokens())
                    {
                        word = tokenizer.nextToken();
                        String noPunct = word.replaceAll("\\p{Punct}", "");
                        String normWord = noPunct.toLowerCase();
                        allWords.add(normWord);
                    }
                }
                reader.close();
            } else  // user closed the file dialog without choosing
            {
                System.out.println("Failed to choose a file to process");
                System.out.println("Run the program again!");
                System.exit(0);
            }
        }
        catch (FileNotFoundException e)
        {
            System.out.println("File not found!!!");
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void noiseFrom()
    {
        JFileChooser chooser = new JFileChooser();
        File selectedFile;
        String rec = "";
        StringTokenizer tokenizer;
        String word = "";
        JOptionPane.showMessageDialog(null, "Please choose a file to extract noise words from", "Choose Noise Words Source", JOptionPane.INFORMATION_MESSAGE);
        try
        {
            File workingDirectory = new File(System.getProperty("user.dir"));

            chooser.setCurrentDirectory(workingDirectory);

            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
            {
                selectedFile = chooser.getSelectedFile();
                Path file = selectedFile.toPath();

                InputStream in =
                        new BufferedInputStream(Files.newInputStream(file, CREATE));
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(in));

                while (reader.ready())
                {
                    rec = reader.readLine();
                    tokenizer = new StringTokenizer(rec);
                    while (tokenizer.hasMoreTokens())
                    {
                        word = tokenizer.nextToken();
                        noiseWords.add(word);
                    }
                }
                reader.close();
            } else  // user closed the file dialog without choosing
            {
                System.out.println("Failed to choose a file to process");
                System.out.println("Run the program again!");
                System.exit(0);
            }
        }
        catch (FileNotFoundException e)
        {
            System.out.println("File not found!!!");
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void excludeNoise()
    {
        for (String noise : noiseWords)
        {
            do
            {
                allWords.remove(noise);
            }
            while(allWords.contains(noise));
        }
    }

    public void displayTagFreq()
    {
        Map<String, Integer> tagFreq = new TreeMap<>();
        for (String word : allWords)
        {
            if (tagFreq.get(word) == null)
            {
                tagFreq.put(word, 1);
            }
            else
            {
                tagFreq.put(word, tagFreq.get(word) + 1);
            }
        }
        for (String word : tagFreq.keySet())
        {
            displayTA.append(word + ": " + tagFreq.get(word) + "\n");
        }
    }

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() -> new TagExtractor());
    }
}
