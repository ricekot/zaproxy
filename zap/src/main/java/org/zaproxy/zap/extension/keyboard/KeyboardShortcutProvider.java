/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2026 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.extension.keyboard;

import java.util.List;
import javax.swing.KeyStroke;
import org.zaproxy.zap.view.ZapMenuItem;

/**
 * Compatibility interface for replacing the legacy keyboard implementation.
 *
 * <p>This is not an action registration API. Implementations own their options UI, configuration,
 * API implementor, and menu discovery. New functionality should be exposed by the providing add-on.
 *
 * @since 2.18.0
 */
public interface KeyboardShortcutProvider {

    /**
     * Installs the replacement after the legacy implementation has been suspended. Called on the
     * EDT. Existing menus must be discovered, including ones registered before this provider was
     * installed.
     */
    void start();

    /**
     * Saves configuration and removes all UI, API, listeners, and bindings owned by this provider.
     * Menu accelerators must be restored to their defaults before returning. Called on the EDT,
     * also after a failed {@link #start()}, so partial initialization must be supported.
     */
    void stop();

    /**
     * Registers or replaces a menu item, preserving bindings keyed by its identifier.
     *
     * @param menuItem the menu item.
     */
    void registerMenuItem(ZapMenuItem menuItem);

    /**
     * Gets a mutable snapshot for legacy callers, including the keyboard cheatsheet API. This
     * method may be called off the EDT.
     *
     * @param defaults whether to return default rather than effective bindings.
     * @return the shortcuts, never {@code null}.
     */
    List<KeyboardShortcut> getShortcuts(boolean defaults);

    /**
     * Gets the effective shortcut for an identifier.
     *
     * @param identifier the identifier.
     * @return the shortcut, or {@code null} if unbound or unknown.
     */
    KeyStroke getShortcut(String identifier);

    /**
     * Sets and records an override for a registered identifier.
     *
     * @param identifier the identifier.
     * @param shortcut the shortcut, or {@code null} to explicitly clear it.
     */
    void setShortcut(String identifier, KeyStroke shortcut);
}
