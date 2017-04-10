package com.company;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

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
        cont.setLayout(new FlowLayout(FlowLayout.CENTER, 10,20));
        input.setPreferredSize(new Dimension(400, 25));
        cont.add(input);
        cont.add(scan);

        cont.add(fileopenButton);
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.getSize(new Dimension(400, 400));
        cont.add(scrollPane);


        fileopenButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileopen = new JFileChooser(); //создаем новый объект JFileChooser
                FileNameExtensionFilter filter = new FileNameExtensionFilter("Text", "txt");
                fileopen.setFileFilter(filter);
                int ret = fileopen.showDialog(null, "Выбрать файл"); //отображаем диалог пользователю.
                if (ret == JFileChooser.APPROVE_OPTION) {
                    File file = fileopen.getSelectedFile();
                    Tokenizator.processFile(file.getName(), file.getPath());
                }
            }
        });
        scan.addActionListener(e -> Tokenizator.findWordsInSameFile(input.getText()));
    }


}
