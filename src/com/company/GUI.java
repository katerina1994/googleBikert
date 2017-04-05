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
    private JPanel cont;


    public GUI() {
        super("GUI");
        this.setBounds(200, 200, 500, 300);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);//позволяет указать действие, которое необходимо выполнить,
        // когда пользователь закрывает окно нажатием на крестик.

        Container cont = this.getContentPane();
        cont.setLayout(new GridLayout(3, 2, 2, 2));// Ширина высота и отступы
        cont.add(input);
        cont.add(scan);

        cont.add(fileopenButton);

        fileopenButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //textField.setText(e.getActionCommand());
                JFileChooser fileopen = new JFileChooser(); //создаем новый объект JFileChooser
                FileNameExtensionFilter filter = new FileNameExtensionFilter("Text", "txt");
                fileopen.setFileFilter(filter);
                int ret = fileopen.showDialog(null, "Выбрать файл"); //отображаем диалог пользователю.
                if (ret == JFileChooser.APPROVE_OPTION) {
                    File file = fileopen.getSelectedFile();
                    String fileName = file.getName();
                    String filePath = file.getPath();
                    Tokenizator.tokenizator(fileName, filePath);
                }
            }
        });
    }


}
