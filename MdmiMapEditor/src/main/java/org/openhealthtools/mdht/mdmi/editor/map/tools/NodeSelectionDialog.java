package org.openhealthtools.mdht.mdmi.editor.map.tools;

import org.openhealthtools.mdht.mdmi.editor.common.Standards;
import org.openhealthtools.mdht.mdmi.editor.common.components.BaseDialog;
import org.openhealthtools.mdht.mdmi.model.Node;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

/**
 * User: AKozyntsev
 * Date: 06.08.12
 */
public class NodeSelectionDialog extends BaseDialog {
    /**
     * Resource for localization
     */
    protected static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.tools.Local");

    //ui components
    private JComboBox m_nodeSelector = new JComboBox();
    private ModelRenderers.NodeRenderer m_nodeRenderer = new ModelRenderers.NodeRenderer();

    //model
    private java.util.List<Node> m_nodes;

    public NodeSelectionDialog(Frame owner, java.util.List<Node> nodes) {
        super(owner, BaseDialog.OK_CANCEL_OPTION);
        m_nodes = nodes;
        buildUI();
        setTitle(s_res.getString("NodeSelectionDialog.title"));
        pack(new Dimension(400, 50));
    }

    private void buildUI() {
        //
        // Route node: [__________________|v]

        m_nodeSelector.setModel(new DefaultComboBoxModel(m_nodes.toArray()));
        m_nodeSelector.setRenderer(m_nodeRenderer);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = Standards.getInsets();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 0;
        gbc.weighty = 0;

        // Root Node
        mainPanel.add(new JLabel(s_res.getString("NodeSelectionDialog.node")), gbc);
        gbc.gridx++;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets.left = 0;
        mainPanel.add(m_nodeSelector, gbc);

        setDirty(true);    // allow OK button

        getContentPane().add(mainPanel, BorderLayout.CENTER);
    }

    @Override
    public void dispose() {
        m_nodeSelector.setRenderer(null);
        super.dispose();
    }

    @Override
    public boolean isDataValid() {
        return true;
    }

    @Override
    protected void okButtonAction() {
        // verify selection
        if (getSelectedNode() == null) {
            JOptionPane.showMessageDialog(this,
                    s_res.getString("NodeSelectionDialog.noSelectionMessage"),
                    s_res.getString("NodeSelectionDialog.noSelectionTitle"),
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        super.okButtonAction();
    }

    public Node getSelectedNode() {
        return (Node) m_nodeSelector.getSelectedItem();
    }
}