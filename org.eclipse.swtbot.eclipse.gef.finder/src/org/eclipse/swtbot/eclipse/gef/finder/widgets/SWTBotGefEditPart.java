/*******************************************************************************
 * Copyright (c) 2004, 2009 MAKE Technologies Inc and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MAKE Technologies Inc - initial API and implementation
 *     Mariot Chauvin <mariot.chauvin@obeo.fr> - refactoring
 *******************************************************************************/
package org.eclipse.swtbot.eclipse.gef.finder.widgets;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.hamcrest.Matcher;

/**
 * represent an edit part of a graphical viewer.
 * 
 * @author David Green
 * 
 * @see SWTBotGefEditor
 */
public class SWTBotGefEditPart {
	protected final EditPart part;
	protected final SWTBotGefEditPart parent;
	protected final SWTBotGefEditor graphicalEditor;
	
	/**
	 * 
	 * @param graphicalViewer 
	 * @param graphicalEditor 
	 * @param parent the parent, or null if this is the root edit part
	 * @param part the GEF part
	 */
	SWTBotGefEditPart(final SWTBotGefEditor graphicalEditor, final SWTBotGefEditPart parent, final EditPart part) {
		this.graphicalEditor = graphicalEditor;
		this.parent = parent;
		this.part = part;
	}

	

	@SuppressWarnings("unchecked")
	public List<SWTBotGefEditPart> children() {
		return UIThreadRunnable.syncExec(new Result<List<SWTBotGefEditPart>>() {
			public List<SWTBotGefEditPart> run() {
				List<SWTBotGefEditPart> children = new ArrayList<SWTBotGefEditPart>();
				for (org.eclipse.gef.EditPart child: ((List<org.eclipse.gef.EditPart>)part.getChildren())) {
					children.add(graphicalEditor.createEditPart(SWTBotGefEditPart.this,child));
				}
				return children;
			}
		});
	}

	/**
	 * find ancestors that match
	 * 
	 * @param matcher the matcher that matches against {@link org.eclipse.gef.EditPart}
	 * 
	 * @return a list of matches or an empty list if there are none
	 */
	@SuppressWarnings("unchecked")
	public List<SWTBotGefEditPart> ancestors(final Matcher<SWTBotGefEditPart> matcher) {
		return  UIThreadRunnable.syncExec(new Result<List<SWTBotGefEditPart>>() {
			public List<SWTBotGefEditPart> run() {
				List<SWTBotGefEditPart> ancestors = new ArrayList<SWTBotGefEditPart>();
				Stack<SWTBotGefEditPart> parts = new Stack<SWTBotGefEditPart>();
				parts.push(SWTBotGefEditPart.this);
				while (!parts.isEmpty()) {
					SWTBotGefEditPart part = parts.pop();
					for (org.eclipse.gef.EditPart child: ((List<org.eclipse.gef.EditPart>) part.part.getChildren())) {
						SWTBotGefEditPart childPart = graphicalEditor.createEditPart(part,child);
						if (matcher.matches(child)) {
							ancestors.add(childPart);
						}
						parts.push(childPart);
					}	
				}
				return ancestors;
			}
		});
	}
	
	//TODO comment
	public EditPart part() {
		return part;
	}

	/**
	 * focus on this edit part
	 */
	public void focus() {
		UIThreadRunnable.syncExec(new VoidResult() {
			public void run() {
				graphicalEditor.graphicalViewer.setFocus(part);
			}
		});
	}
	
	/**
	 * select this edit part as a single selection
	 */
	public SWTBotGefEditPart select() {
		graphicalEditor.select(this);
		return this;
	}
	
	/**
	 * click on the edit part
	 */
	public SWTBotGefEditPart click() {
		UIThreadRunnable.syncExec(new VoidResult() {
			public void run() {
				IFigure figure = ((GraphicalEditPart) part).getFigure();
				Rectangle bounds = figure.getBounds().getCopy();
				figure.translateToAbsolute(bounds);
				graphicalEditor.eventDispatcher.dispatchMouseEntered(createMouseEvent(0, 0, bounds.x, bounds.y));
				graphicalEditor.eventDispatcher.dispatchMouseMoved(createMouseEvent(0, 0, bounds.x, bounds.y));
				graphicalEditor.eventDispatcher.dispatchMousePressed(createMouseEvent(1, 1, bounds.x, bounds.y));
				graphicalEditor.eventDispatcher.dispatchMouseReleased(createMouseEvent(1, 1, bounds.x, bounds.y));
				graphicalEditor.eventDispatcher.dispatchMouseExited(createMouseEvent(0, 0, bounds.x, bounds.y));
			}		
		});
		return this;
	}

	private org.eclipse.swt.events.MouseEvent createMouseEvent(int button,int count,int x, int y) {
		Event e = createEvent();
		e.button = button;
		e.count = count;
		e.x = x;
		e.y = y;
		org.eclipse.swt.events.MouseEvent event = new org.eclipse.swt.events.MouseEvent(e);
		return event; 
	}

	private Event createEvent() {
		IFigure figure = ((GraphicalEditPart)part).getFigure();
		Point point = new Point(0,0);
		figure.translateToAbsolute(point);
		Event e = new Event();
		e.display = Display.getCurrent();
		e.time = (int) System.currentTimeMillis();
		e.widget = graphicalEditor.graphicalViewer.getControl();
		return e;
	}

	
	
	/**
	 * get the parent, or null if this is the root edit part.
	 */
	public SWTBotGefEditPart parent() {
		return parent; 
	}
	
	/**
	 * provide a description of this edit part that is useful for debugging purposes.
	 */
	public String toString() {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		
		describe(out,0);
		
		out.close();
		return writer.toString();
	}


	private void describe(PrintWriter out,int indent) {
		out.print(indent(indent));
		out.print(part.getClass().getName());
		List<SWTBotGefEditPart> children = children();
		out.print(" children="+children.size());
		out.println();
		for (SWTBotGefEditPart child: children) {
			child.describe(out, indent+1);
		}
	}
	
	private String indent(int size) {
		if (size == 0) {
			return "";
		}
		StringBuilder buf = new StringBuilder(size);
		for (int x = 0;x<size;++x) {
			buf.append("\t");
		}
		return buf.toString();
	}

	@SuppressWarnings("unchecked")
	public List<SwtBotGefConnectionEditPart> sourceConnections() {
		return UIThreadRunnable.syncExec(new Result<List<SwtBotGefConnectionEditPart>>() {
			public List<SwtBotGefConnectionEditPart> run() {
				List<SwtBotGefConnectionEditPart> connections = new ArrayList<SwtBotGefConnectionEditPart>();
				List<org.eclipse.gef.ConnectionEditPart> sourceConnections = ((GraphicalEditPart)part).getSourceConnections();
				for (org.eclipse.gef.ConnectionEditPart c: sourceConnections) {
					connections.add(graphicalEditor.createEditPart(c));
				}
				return connections;
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	public List<SwtBotGefConnectionEditPart> targetConnections() {
		return UIThreadRunnable.syncExec(new Result<List<SwtBotGefConnectionEditPart>>() {
			public List<SwtBotGefConnectionEditPart> run() {
				List<SwtBotGefConnectionEditPart> connections = new ArrayList<SwtBotGefConnectionEditPart>();
				List<org.eclipse.gef.ConnectionEditPart> targetConnections = ((GraphicalEditPart)part).getTargetConnections();
				for (org.eclipse.gef.ConnectionEditPart c: targetConnections) {
					connections.add(graphicalEditor.createEditPart(c));
				}
				return connections;
			}
		});
	}	
}
