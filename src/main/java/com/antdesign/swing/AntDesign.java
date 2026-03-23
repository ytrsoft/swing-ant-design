package com.antdesign.swing;

import com.antdesign.swing.base.AbstractAntFrame;
import com.antdesign.swing.general.AntPanel;
import com.antdesign.swing.general.AntText;

/**
 *  程序入口
 *
 */
public class AntDesign extends AbstractAntFrame {

    @Override
    protected void initContent() {
        super.initContent();
        AntText text = new AntText("Hello World");
        AntPanel panel = new AntPanel();
        panel.add(text);
        getContentPane().add(panel);
    }

    public static void main(String[] args) {
        launch();
    }
}
