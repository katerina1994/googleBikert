package com.company;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.*;
import java.util.List;

public class GUI extends JFrame {

    private JButton scan = new JButton("Поиск");
    private JButton fileopenButton = new JButton("Выбрать файл");
    private JTextField input = new JTextField("");
    private JTextArea textAreaFile = new JTextArea();
    private JTextArea textAreaQuote = new JTextArea();
    private JPanel cont;


    public GUI() {
        super("GUI");
        this.setBounds(200, 200, 700, 100);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);//позволяет указать действие, которое необходимо выполнить,
        // когда пользователь закрывает окно нажатием на крестик.

        Container cont = this.getContentPane();
        //cont.setLayout(new GridLayout(1, 3, 20, 20));// Ширина высота и отступы
        cont.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 20));
        input.setPreferredSize(new Dimension(400, 25));
        cont.add(input);
        cont.add(scan);

        cont.add(fileopenButton);

        JFrame frame = new JFrame();
        JEditorPane textarea = new JEditorPane("text/html", "");


//        textarea.setText("<b>Bold</b> and normal text");


        JScrollPane scrollPane = new JScrollPane(textarea);

        frame.add(scrollPane);
        frame.setBounds(200, 300,700,300);
        //frame.setSize(200, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(false);
//        textarea.setText("<b>Bold</b> and normal text" + Math.random());

        fileopenButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileopen = new JFileChooser(); //создаем новый объект JFileChooser
                FileNameExtensionFilter filter = new FileNameExtensionFilter("Text", "txt");
                fileopen.setFileFilter(filter);
                int ret = fileopen.showDialog(null, "Выбрать файл"); //отображаем диалог пользователю.
                if (ret == JFileChooser.APPROVE_OPTION) {
                    File file = fileopen.getSelectedFile();
                    Tokenizer.processFile(file.getName(), file.getPath());
                }
            }
        });

        input.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    scan.doClick();
            }

            public void keyTyped(KeyEvent e) {
            }

            public void keyReleased(KeyEvent e) {
            }
        });
        scan.addActionListener(e -> {
            new Thread(() -> {
                String str = input.getText();
                Tokenizer.findWordsInSameFile(str);
                frame.setVisible(true);
                Map<String, Map<Integer, List<String>>> resultHashMap = Tokenizer.findWordsInSameLine(str);
                StringBuilder message = new StringBuilder();
                for (Map.Entry<String, Map<Integer, List<String>>> fileEntry :
                        resultHashMap.entrySet()) {
                    message.append("File :").append(fileEntry.getKey()).append("<br>");
                    for (Map.Entry<Integer, List<String>> lineEntry :
                            fileEntry.getValue().entrySet()) {
                        message.append("Line :").append(lineEntry.getKey()).append("<br>");
                        for (String s :
                                lineEntry.getValue()) {
                            message.append("Quote: ").append(s).append("<br>");
                        }
                    }
                }
                textarea.setText(message.toString());
                textarea.setCaretPosition(0);
                frame.toFront();
                frame.repaint();
            }).start();
        });
    }


}
