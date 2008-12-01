/*
 * This file is part of the Alchemy project - http://al.chemy.org
 * 
 * Copyright (c) 2007-2008 Karl D.D. Willis
 * 
 * Alchemy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alchemy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Alchemy.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.alchemy.core;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;

/**
 * AlcColour Selector
 * Based on code from the lovely Processing ColorSelector [sic]
 * http://dev.processing.org/source/index.cgi/trunk/processing/app/src/processing/app/tools/ColorSelector.java?view=markup
 */
public class AlcColourSelector extends JFrame implements DocumentListener, AlcConstants {

    private int hue,  saturation,  brightness;  // range 360, 100, 100
    private int red,  green,  blue;   // range 256, 256, 256
    private ColourRange range;
    private ColourSlider slider;
    private JTextField hueField,  saturationField,  brightnessField;
    private JTextField redField,  greenField,  blueField;
    private JTextField hexField;
    private JPanel colourPanel;
    private JButton okButton,  cancelButton;
    private boolean updating;
    private int labelH;

    AlcColourSelector(String title) {

        super(title);

        this.getContentPane().setLayout(new BorderLayout());

        Box box = Box.createHorizontalBox();
        box.setBorder(new EmptyBorder(12, 12, 12, 12));

        range = new ColourRange();

        Box rangeBox = new Box(BoxLayout.Y_AXIS);
        rangeBox.setAlignmentY(0);
        rangeBox.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        rangeBox.add(range);
        box.add(rangeBox);
        box.add(Box.createHorizontalStrut(10));

        slider = new ColourSlider();

        Box sliderBox = new Box(BoxLayout.Y_AXIS);
        sliderBox.setAlignmentY(0);
        sliderBox.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        sliderBox.add(slider);
        box.add(sliderBox);
        box.add(Box.createHorizontalStrut(10));

        box.add(createColorFields());
        box.add(Box.createHorizontalStrut(10));


        this.getContentPane().add(box, BorderLayout.CENTER);
        // TODO - Align the button pane to the bottom
        this.getContentPane().add(createButtonPanel(), BorderLayout.SOUTH);

        this.pack();
        this.setResizable(false);
        this.setLocationRelativeTo(null);

        this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

        AlcUtil.registerWindowCloseKeys(this.getRootPane(), new AbstractAction() {

            public void actionPerformed(ActionEvent actionEvent) {
                setVisible(false);
            }
        });

        hueField.getDocument().addDocumentListener(this);
        saturationField.getDocument().addDocumentListener(this);
        brightnessField.getDocument().addDocumentListener(this);
        redField.getDocument().addDocumentListener(this);
        greenField.getDocument().addDocumentListener(this);
        blueField.getDocument().addDocumentListener(this);
        hexField.getDocument().addDocumentListener(this);

        hexField.setText("FFFFFF");
    }

    /** Creates and shows a JDialog with the Alchemy colour pane and the given actions 
     * 
     * @param okListener        The ActionListener for the OK button
     * @param cancelListener    The ActionListener for the CANCEL button
     */
    public void show(ActionListener okListener, ActionListener cancelListener) {
        show(okListener, cancelListener, null);
    }

    /** Creates and shows a JDialog with the Alchemy colour pane and the given actions 
     * 
     * @param okListener        The ActionListener for the OK button
     * @param cancelListener    The ActionListener for the CANCEL button
     * @param initialColour     The initial colour to display
     */
    public void show(ActionListener okListener, ActionListener cancelListener, Color initialColour) {
        okButton.addActionListener(okListener);
        cancelButton.addActionListener(cancelListener);
        if (initialColour != null) {
            setColour(initialColour);
        }
        range.init = true;
        this.setVisible(true);
    }

    /**  Gets the current color value from the colour selector
     * @return  The current colour value
     */
    public Color getColour() {
        return new Color(red, green, blue);
    }

    public void setColour(Color colour) {
        updateRGB2(colour.getRGB());
        updateHSB();
        updateHex();
        range.repaint();
        slider.repaint();
        colourPanel.repaint();
    }

    public void changedUpdate(DocumentEvent e) {
    }

    public void removeUpdate(DocumentEvent e) {
    }

    public void insertUpdate(DocumentEvent e) {
        if (updating) {
            return;  // don't update forever recursively
        }
        updating = true;

        Document doc = e.getDocument();
        if (doc == hueField.getDocument()) {
            hue = bounded(hue, hueField, 359);
            updateRGB();
            updateHex();

        } else if (doc == saturationField.getDocument()) {
            saturation = bounded(saturation, saturationField, 99);
            updateRGB();
            updateHex();

        } else if (doc == brightnessField.getDocument()) {
            brightness = bounded(brightness, brightnessField, 99);
            updateRGB();
            updateHex();

        } else if (doc == redField.getDocument()) {
            red = bounded(red, redField, 255);
            updateHSB();
            updateHex();

        } else if (doc == greenField.getDocument()) {
            green = bounded(green, greenField, 255);
            updateHSB();
            updateHex();

        } else if (doc == blueField.getDocument()) {
            blue = bounded(blue, blueField, 255);
            updateHSB();
            updateHex();

        } else if (doc == hexField.getDocument()) {
            String str = hexField.getText();
            while (str.length() < 6) {
                str += "0";
            }
            if (str.length() > 6) {
                str = str.substring(0, 6);
            }
            updateRGB2(Integer.parseInt(str, 16));
            updateHSB();
        }
        range.repaint();
        slider.repaint();
        colourPanel.repaint();
        updating = false;
    }

    /**
     * Set the RGB values based on the current HSB values.
     */
    private void updateRGB() {
        int rgb = Color.HSBtoRGB((float) hue / 359f,
                (float) saturation / 99f,
                (float) brightness / 99f);
        updateRGB2(rgb);
    }

    /**
     * Set the RGB values based on a calculated ARGB int.
     * Used by both updateRGB() to set the color from the HSB values,
     * and by updateHex(), to unpack the hex colors and assign them.
     * @param rgb 
     */
    private void updateRGB2(int rgb) {
        red = (rgb >> 16) & 0xff;
        green = (rgb >> 8) & 0xff;
        blue = rgb & 0xff;

        redField.setText(String.valueOf(red));
        greenField.setText(String.valueOf(green));
        blueField.setText(String.valueOf(blue));
    }

    /**
     * Set the HSB values based on the current RGB values.
     */
    private void updateHSB() {
        float hsb[] = new float[3];
        Color.RGBtoHSB(red, green, blue, hsb);

        hue = (int) (hsb[0] * 359.0f);
        saturation = (int) (hsb[1] * 99.0f);
        brightness = (int) (hsb[2] * 99.0f);

        hueField.setText(String.valueOf(hue));
        saturationField.setText(String.valueOf(saturation));
        brightnessField.setText(String.valueOf(brightness));
    }

    private void updateHex() {
        hexField.setText(AlcUtil.hex(red, 2) +
                AlcUtil.hex(green, 2) +
                AlcUtil.hex(blue, 2));
    }

    /**
     * Get the bounded value for a specific range. If the value is outside
     * the max, you can't edit right away, so just act as if it's already
     * been bounded and return the bounded value, then fire an event to set
     * it to the value that was just returned.
     * @param current
     * @param field 
     * @param max
     * @return 
     */
    private int bounded(int current, final JTextField field, final int max) {
        String text = field.getText();
        if (text.length() == 0) {
            return 0;
        }
        try {
            int value = Integer.parseInt(text);
            if (value > max) {
                SwingUtilities.invokeLater(new  

                      Runnable() {

                        
                    
                
                 
            
             
            
             
        
    

    public    
           
        void run() {
                        field.setText(String.valueOf(max));
                    }
                });
                return max;
            }
            return value;

        } catch (NumberFormatException e) {
            return current;  // should not be reachable
        }
    }

    private Container createColorFields() {
        Box box = Box.createVerticalBox();
        box.setAlignmentY(0);

        colourPanel = new  

              JPanel( ) {

                   
                   
                @Override
            public void paintComponent(Graphics g) {
                g.setColor(new Color(red, green, blue));
                Dimension size = getSize();
                g.fillRect(0, 0, size.width, size.height);
            }
        };
        colourPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        Dimension dim = new Dimension(60, 40);
        colourPanel.setMinimumSize(dim);
        //colorPanel.setMaximumSize(dim);
        //colorPanel.setPreferredSize(dim);
        box.add(colourPanel);
        box.add(Box.createVerticalStrut(15));

        Box row = Box.createHorizontalBox();

        row.add(createFixedLabel("H:"));
        row.add(hueField = new NumberField(4, false));
        row.add(new JLabel(" \u00B0"));  // degree symbol
        row.add(Box.createHorizontalGlue());
        box.add(row);
        //box.add(Box.createVerticalStrut(5));

        row = Box.createHorizontalBox();
        row.add(createFixedLabel("S:"));
        row.add(saturationField = new NumberField(4, false));
        row.add(new JLabel(" %"));
        row.add(Box.createHorizontalGlue());
        box.add(row);
        //box.add(Box.createVerticalStrut(5));

        row = Box.createHorizontalBox();
        row.add(createFixedLabel("B:"));
        row.add(brightnessField = new NumberField(4, false));
        row.add(new JLabel(" %"));
        row.add(Box.createHorizontalGlue());
        box.add(row);

        box.add(Box.createVerticalStrut(10));

        // RGB
        row = Box.createHorizontalBox();
        row.add(createFixedLabel("R:"));
        row.add(redField = new NumberField(4, false));
        row.add(Box.createHorizontalGlue());
        box.add(row);
        //box.add(Box.createVerticalStrut(5));

        row = Box.createHorizontalBox();
        row.add(createFixedLabel("G:"));
        row.add(greenField = new NumberField(4, false));
        row.add(Box.createHorizontalGlue());
        box.add(row);
        //box.add(Box.createVerticalStrut(5));

        row = Box.createHorizontalBox();
        row.add(createFixedLabel("B:"));
        row.add(blueField = new NumberField(4, false));
        row.add(Box.createHorizontalGlue());
        box.add(row);

        box.add(Box.createVerticalStrut(10));

        // HEX
        row = Box.createHorizontalBox();
        row.add(createFixedLabel("#"));
        row.add(hexField = new NumberField(5, true));
        row.add(Box.createHorizontalGlue());
        box.add(row);
        //box.add(Box.createVerticalStrut(10));

        box.add(Box.createVerticalGlue());
        return box;
    }

    private JPanel createButtonPanel() {

        // Reset Button
//        JButton resetButton = new JButton(Alchemy.bundle.getString("reset"));
//        resetButton.addActionListener(
//                new ActionListener() {
//
//                    public void actionPerformed(ActionEvent e) {
//                        //
//                    }
//                });

        // Cancel Button
        cancelButton = new JButton(Alchemy.bundle.getString("cancel"));
        cancelButton.setMnemonic(KeyEvent.VK_ESCAPE);
        cancelButton.addActionListener(
                new  

                      ActionListener( ) {

                        public void actionPerformed(ActionEvent e) {
                        setVisible(false);
                    }
                });

        // Ok Button
        okButton = new JButton(Alchemy.bundle.getString("ok"));
        okButton.setMnemonic(KeyEvent.VK_ENTER);
        okButton.addActionListener(
                new  

                      ActionListener( ) {

                        public void actionPerformed(ActionEvent e) {
                        setVisible(false);
                    }
                });

        JPanel buttonPane = new JPanel();
        buttonPane.setOpaque(false);
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
//        buttonPane.add(resetButton);
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(cancelButton);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(okButton);

        return buttonPane;
    }

    /** Return a label of a fixed width     
     * 
     * @param title
     * @return
     */
    private JLabel createFixedLabel(String title) {
        JLabel label = new JLabel(title);
        if (labelH == 0) {
            labelH = label.getPreferredSize().height;
        }
        Dimension dim = new Dimension(20, labelH);
        label.setPreferredSize(dim);
        label.setMinimumSize(dim);
        label.setMaximumSize(dim);
        return label;
    }

    /** ColourRange panel to select a colour visually */
    class ColourRange extends JPanel implements MouseListener, MouseMotionListener {

        static final int WIDE = 256;
        static final int HIGH = 256;
         
         
         int lastX     ,  lastY      ;
           private int pixels[] = new int[WIDE * HIGH];
        private BufferedImage colourArray = new BufferedImage(WIDE, HIGH, BufferedImage.TYPE_INT_ARGB);
        boolean init = true;

        ColourRange() {
            this.setPreferredSize(new Dimension(WIDE, HIGH));
            this.setCursor(CURSOR_CIRCLE);
            this.addMouseListener(this);
            this.addMouseMotionListener(this);
        }

        @Override
        protected void paintComponent(Graphics g) {

            int index = 0;
            float fhue = hue / 359f;
            Color targetColour = new Color(red, green, blue);
            for (int j = 0; j < 256; j++) {
                for (int i = 0; i < 256; i++) {
                    Color c = Color.getHSBColor(fhue, i / 255f, (255 - j) / 255f);
                    if (init) {
                        if (nearlyEquals(targetColour, c)) {
                            lastX = i;
                            lastY = j;
                            //System.out.println("HIT: " + lastX + " " + lastY);
                            init = false;
                        }
                    }
                    pixels[index++] = c.getRGB();
                }
            }

            // Make the colour array into an image
            colourArray.setRGB(0, 0, WIDE, HIGH, pixels, 0, WIDE);
            Graphics2D g2 = (Graphics2D) g;
            g2.drawImage(colourArray, 0, 0, null);

            g2.setColor((brightness > 50) ? Color.BLACK : Color.WHITE);
            g2.drawRect(lastX - 4, lastY - 4, 8, 8);
        }

        /** Work out if two colours are nearly equal */
        private boolean nearlyEquals(Color c1, Color c2) {
            boolean result = false;
            final int difference = 1;
            if (c1.equals(c2)) {
                result = true;
            } else {
                int red = Math.abs(c1.getRed() - c2.getRed());
                int green = Math.abs(c1.getGreen() - c2.getGreen());
                int blue = Math.abs(c1.getBlue() - c2.getBlue());
                if (red + green + blue <= difference) {
                    result = true;
                }
            }
            return result;
        }

        void updateMouse(int mouseX, int mouseY) {
            if ((mouseX >= 0) && (mouseX < 256) &&
                    (mouseY >= 0) && (mouseY < 256)) {
                int nsaturation = (int) (100 * (mouseX / 255.0f));
                int nbrightness = 100 - ((int) (100 * (mouseY / 255.0f)));
                saturationField.setText(String.valueOf(nsaturation));
                brightnessField.setText(String.valueOf(nbrightness));

                lastX = mouseX;
                lastY = mouseY;
            }
        }

        @Override
        public Dimension getPreferredSize() {
            //System.out.println("getting pref " + WIDE + " " + HIGH);
            return new Dimension(WIDE, HIGH);
        }

        @Override
        public Dimension getMinimumSize() {
            //System.out.println("getting min " + WIDE + " " + HIGH);
            return new Dimension(WIDE, HIGH);
        }

        @Override
        public Dimension getMaximumSize() {
            //System.out.println("getting max " + WIDE + " " + HIGH);
            return new Dimension(WIDE, HIGH);
        }

        public void mouseClicked(MouseEvent e) {
        }

        public void mousePressed(MouseEvent e) {
            this.updateMouse(e.getX(), e.getY());
        }

        public void mouseReleased(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        public void mouseDragged(MouseEvent e) {
            this.updateMouse(e.getX(), e.getY());
        }

        public void mouseMoved(MouseEvent e) {
        }
    }

    /** ColourSlider class to select a hue */
    class ColourSlider extends JPanel implements MouseListener, MouseMotionListener {

        static final int WIDE = 20;
        static final int HIGH = 256;
        private int pixels[] = new int[WIDE * HIGH];
        private BufferedImage colourArray = new BufferedImage(WIDE, HIGH, BufferedImage.TYPE_INT_ARGB);

        ColourSlider() {
            this.setPreferredSize(new Dimension(WIDE, HIGH));
            this.addMouseListener(this);
            this.addMouseMotionListener(this);

        }

        @Override
        protected void paintComponent(Graphics g) {

            int index = 0;
            int sel = 255 - (int) (255 * (hue / 359f));
            for (int j = 0; j < 256; j++) {
                Color c = Color.getHSBColor((255f - j) / 255f, 1f, 1f);
                if (j == sel) {
                    c = new Color(0xFF000000);
                }
                for (int i = 0; i < WIDE; i++) {
                    pixels[index++] = c.getRGB();
                }
            }
            // Make the colour array into an image
            colourArray.setRGB(0, 0, WIDE, HIGH, pixels, 0, WIDE);
            Graphics2D g2 = (Graphics2D) g;
            g2.drawImage(colourArray, 0, 0, null);

        }

        void updateMouse(int mouseX, int mouseY) {

            if ((mouseX >= 0) && (mouseX < 256) &&
                    (mouseY >= 0) && (mouseY < 256)) {
                int nhue = 359 - (int) (359 * (mouseY / 255.0f));
                hueField.setText(String.valueOf(nhue));
            }
        }

        @Override
        public Dimension getPreferredSize() {
            //System.out.println("s getting pref " + WIDE + " " + HIGH);
            return new Dimension(WIDE, HIGH);
        }

        @Override
        public Dimension getMinimumSize() {
            //System.out.println("s getting min " + WIDE + " " + HIGH);
            return new Dimension(WIDE, HIGH);
        }

        @Override
        public Dimension getMaximumSize() {
            //System.out.println("s getting max " + WIDE + " " + HIGH);
            return new Dimension(WIDE, HIGH);
        }

        public void mouseClicked(MouseEvent e) {
        }

        public void mousePressed(MouseEvent e) {
            updateMouse(e.getX(), e.getY());
        }

        public void mouseReleased(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        public void mouseDragged(MouseEvent e) {
            updateMouse(e.getX(), e.getY());
        }

        public void mouseMoved(MouseEvent e) {
        }
    }

    /**
     * Extension of JTextField that only allows numbers
     */
    class NumberField extends JTextField {

        public boolean allowHex;

        public NumberField(int cols, boolean allowHex) {
            super(cols);
            this.allowHex = allowHex;
        }

        @Override
        protected Document createDefaultModel() {
            return new NumberDocument(this);
        }

        @Override
        public Dimension getPreferredSize() {
            if (!allowHex) {
                return new Dimension(45, super.getPreferredSize().height);
            }
            return super.getPreferredSize();
        }

        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }
    }

    /**
     * Document model to go with JTextField that only allows numbers.
     */
    class NumberDocument extends PlainDocument {

        NumberField parentField;

        public NumberDocument(NumberField parentField) {
            this.parentField = parentField;
        //System.out.println("setting parent to " + parentSelector);
        }

        @Override
        public void insertString(int offs, String str, AttributeSet a)
                throws BadLocationException {

            if (str == null) {
                return;
            }
            char chars[] = str.toCharArray();
            int charCount = 0;
            // remove any non-digit chars
            for (int i = 0; i < chars.length; i++) {
                boolean ok = Character.isDigit(chars[i]);
                if (parentField.allowHex) {
                    if ((chars[i] >= 'A') && (chars[i] <= 'F')) {
                        ok = true;
                    }
                    if ((chars[i] >= 'a') && (chars[i] <= 'f')) {
                        ok = true;
                    }
                }
                if (ok) {
                    if (charCount != i) {  // shift if necessary
                        chars[charCount] = chars[i];
                    }
                    charCount++;
                }
            }
            super.insertString(offs, new String(chars, 0, charCount), a);
        // can't call any sort of methods on the enclosing class here
        // seems to have something to do with how Document objects are set up
        }
    }
}