/*
 *  This file is part of the Alchemy project - http://al.chemy.org
 * 
 *  Copyright (c) 2007 Karl D.D. Willis
 * 
 *  Alchemy is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  Alchemy is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with Alchemy.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package org.alchemy.core;

import java.awt.Graphics;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.KeyStroke;

class AlcCheckBoxMenuItem extends JCheckBoxMenuItem implements AlcConstants {

    private int index;
    private int moduleType = -1;
    private static int checkX;

    static {
        if (Alchemy.PLATFORM == MACOSX) {
            checkX = 6;
        } else {
            checkX = 4;
        }
    }

    AlcCheckBoxMenuItem() {
    }

    AlcCheckBoxMenuItem(Action action) {
        this.setAction(action);
    }

    AlcCheckBoxMenuItem(String title) {
        setup(title, -1);
    }

    AlcCheckBoxMenuItem(String title, int accelerator) {
        setup(title, accelerator);
    }

    AlcCheckBoxMenuItem(AlcModule module) {

        setup(module.getName(), -1);
        this.index = module.getIndex();
        this.moduleType = module.getModuleType();

        // Set the intial state to false
        //this.setState(true);

        // Set the main Icon
        this.setIcon(AlcUtil.createImageIcon(module.getIconUrl()));
    }

    void setup(String title) {
        setup(title, -1);
    }

    void setup(String title, int accelerator) {

        this.setText(title);

        // Top Left Bottom Right
        this.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
        //this.setOpaque(true);
        this.setBackground(AlcToolBar.toolBarHighlightColour);
        this.setFont(AlcToolBar.toolBarFont);

        if (accelerator > 0) {
            this.setAccelerator(KeyStroke.getKeyStroke(accelerator, MENU_SHORTCUT));
        }
    }

    int getIndex() {
        return index;
    }

    int getModuleType() {
        return moduleType;
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (!this.getState()) {
            g.setColor(AlcToolBar.toolBarBoxColour);
            // This is the toolbar menu popup
            if (moduleType > 0) {
                g.drawRect(checkX, 16, 7, 7);

            // This is the menubar
            } else {
                g.drawRect(checkX, 10, 7, 7);
            }
        }
    }
}