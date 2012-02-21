/**
 *  Catroid: An on-device graphical programming language for Android devices
 *  Copyright (C) 2010-2011 The Catroid Team
 *  (<http://code.google.com/p/catroid/wiki/Credits>)
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *  
 *  An additional term exception under section 7 of the GNU Affero
 *  General Public License, version 3, is available at
 *  http://www.catroid.org/catroid_license_additional_term
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *   
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package at.tugraz.ist.catroid.ui.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import at.tugraz.ist.catroid.ProjectManager;
import at.tugraz.ist.catroid.R;
import at.tugraz.ist.catroid.content.BroadcastScript;
import at.tugraz.ist.catroid.content.Script;
import at.tugraz.ist.catroid.content.Sprite;
import at.tugraz.ist.catroid.content.StartScript;
import at.tugraz.ist.catroid.content.WhenScript;
import at.tugraz.ist.catroid.content.bricks.Brick;
import at.tugraz.ist.catroid.content.bricks.BroadcastReceiverBrick;
import at.tugraz.ist.catroid.content.bricks.LoopBeginBrick;
import at.tugraz.ist.catroid.content.bricks.LoopEndBrick;
import at.tugraz.ist.catroid.content.bricks.WhenBrick;
import at.tugraz.ist.catroid.content.bricks.WhenStartedBrick;
import at.tugraz.ist.catroid.ui.ScriptActivity;
import at.tugraz.ist.catroid.ui.dragndrop.DragAndDropListView;
import at.tugraz.ist.catroid.ui.dragndrop.DragAndDropListener;

public class BrickAdapter extends BaseAdapter implements DragAndDropListener {

	public static final int FOCUS_BLOCK_DESCENDANTS = 2;

	private Context context;
	private Sprite sprite;
	// private BrickListAnimation brickListAnimation;
	private int dragTargetPosition;
	private Brick draggedBrick;
	private OnLongClickListener longClickListener;
	private View insertionView;
	private int currentScriptPosition;
	private boolean insertedBrick;
	private boolean insertLoop;
	private int pos;
	private int fromTest;

	public BrickAdapter(Context context, Sprite sprite, DragAndDropListView listView) {
		this.context = context;
		this.sprite = sprite;
		// brickListAnimation = new BrickListAnimation(this, listView);
		longClickListener = listView;
		insertionView = View.inflate(context, R.layout.brick_insert, null);
		insertedBrick = false;
		insertLoop = false;
	}

	public void drag(int from, int to) {
		Log.d("TESTING", "drag: from " + from + " to " + to);
		fromTest = from;
		int scriptFrom = getScriptId(from);
		int scriptTo = getScriptId(to);
		//		Log.d("TESTING", "Drag called from: " + from + ", to: " + to);

		if (isBrick(to)) {
			if (draggedBrick == null) {
				if (isBrick(from)) {
					draggedBrick = (Brick) getItem(from);
				} else {
					Log.d("Warning", "BrickAdapter.drag() from was Script not Brick. should not happen!!!");
				}
				notifyDataSetChanged();
			}

			if (to != 0) {
				dragTargetPosition = to;
			} else {
				dragTargetPosition = 1;
			}

			ArrayList<Brick> brickList = sprite.getScript(getScriptId(from)).getBrickList();
			if (draggedBrick instanceof LoopBeginBrick) {
				LoopEndBrick loopEndBrick = ((LoopBeginBrick) draggedBrick).getLoopEndBrick();
				if (loopEndBrick != null) {
					if (getScriptPosition(to, scriptTo) >= brickList.indexOf(loopEndBrick)
							|| getScriptPosition(from, scriptFrom) >= brickList.indexOf(loopEndBrick)) {
						return;
					}
				} else {
					insertLoop = true;
				}
			} else if (draggedBrick instanceof LoopEndBrick) {
				LoopBeginBrick loopBeginBrick = ((LoopEndBrick) draggedBrick).getLoopBeginBrick();

				Script script = ProjectManager.getInstance().getCurrentSprite().getScript(scriptTo);
				if (script.getBrickList().indexOf(((LoopEndBrick) draggedBrick).getLoopBeginBrick()) == -1) {
					dragTargetPosition = -1;
					return;
				}

				if (getScriptPosition(to, scriptTo) <= brickList.indexOf(loopBeginBrick)
						|| getScriptPosition(from, scriptFrom) <= brickList.indexOf(loopBeginBrick)) {
					return;
				}
			}

			if (from != to) {
				Log.d("TESTING", "bricks lol: ");
				sprite.getScript(scriptFrom).removeBrick(draggedBrick);
				sprite.getScript(scriptTo).addBrick(getScriptPosition(to, scriptTo), draggedBrick);
			}

			Log.d("TESTING", "bricks lol: dragTargetPosition " + dragTargetPosition);

		} else {
			if (to != 0) {
				dragTargetPosition = to;
			} else {
				dragTargetPosition = 1;

			}

			if (from < to) {

				if (draggedBrick instanceof LoopEndBrick) {
					dragTargetPosition = -1;
					return;
				}
				sprite.getScript(getScriptId(to)).addBrick(0, draggedBrick);
				sprite.getScript(scriptFrom).removeBrick(draggedBrick);
				if (currentScriptPosition != 0 && from < currentScriptPosition) {
					currentScriptPosition--;
				}
			} else if (from > to && to > 0) {

				if (draggedBrick instanceof LoopEndBrick) {
					dragTargetPosition = -1;
					return;
				}

				sprite.getScript(getScriptId(to) - 1).addBrick(
						sprite.getScript(getScriptId(to) - 1).getBrickList().size(), draggedBrick);
				sprite.getScript(scriptFrom).removeBrick(draggedBrick);
				if (!(currentScriptPosition < to)) {
					currentScriptPosition++;
				}
			} else {
				if (draggedBrick instanceof LoopEndBrick) {
					dragTargetPosition = -1;
					//					int nrScripts = ProjectManager.getInstance().getCurrentSprite().getNumberOfScripts();
					//					int bId = 1;
					//					boolean breakLoop = false;
					//
					//					for (int i = 0; i < nrScripts; i++) {
					//						if (breakLoop) {
					//							break;
					//						}
					//						ArrayList<Brick> bricks = ProjectManager.getInstance().getCurrentSprite().getScript(i)
					//								.getBrickList();
					//						for (int j = 0; j < bricks.size(); j++) {
					//							if (bricks.get(j) instanceof LoopBeginBrick) {
					//								breakLoop = true;
					//								break;
					//							}
					//							bId++;
					//						}
					//						bId++;
					//					}
					//
					//										ArrayList<Brick> bricks = ProjectManager.getInstance().getCurrentSprite().getScript(sId)
					//												.getBrickList();
					//										for (int i = 0; i < bricks.size(); i++) {
					//											if (bricks.get(i) instanceof LoopBeginBrick) {
					//												bId = i + 1;
					//											}
					//										}
					//
					//										bId = ProjectManager.getInstance().getCurrentSprite().getScript(1).getBrickList()
					//												.indexOf(draggedBrick) + 3;
					//
					//					dragTargetPosition = bId;
				}
			}
		}

		notifyDataSetChanged();
	}

	public void drop(int to) {
		boolean toLastScript = false;
		Log.d("TESTING", "drop: " + to);

		if (to < 0) {
			Log.d("TESTING", "dragTargetPosition: " + dragTargetPosition);
			int nrScripts = ProjectManager.getInstance().getCurrentSprite().getNumberOfScripts();

			int newTo = 0;
			Log.d("TESTING", "nrScripts: " + nrScripts);

			for (int i = 0; i < nrScripts; i++) {
				int tmp = ProjectManager.getInstance().getCurrentSprite().getScript(i).getBrickList().size();

				if (tmp == 0) {
					tmp++;
				}

				newTo += tmp;
				Log.d("TESTING", "foo: " + tmp);
			}
			to = newTo;
			//to = dragTargetPosition;
			Log.d("TESTING", "Itemposition: " + to);
			toLastScript = true;
		}

		if (draggedBrick instanceof WhenBrick) {

			int sId = getScriptId(to);
			ProjectManager projectManager = ProjectManager.getInstance();
			Script newScript = new WhenScript(projectManager.getCurrentSprite());
			if (toLastScript) {
				projectManager.getCurrentSprite().addScript(newScript);
			} else {
				ArrayList<Brick> bricks = projectManager.getCurrentSprite().getScript(sId).getBrickList();

				ArrayList<Brick> tmpList = new ArrayList<Brick>();

				int brickToScript = 0;
				for (Brick brick : bricks) {

					if (brick instanceof WhenBrick) {
						projectManager.getCurrentSprite().getScript(sId).removeBrick(brick);
						break;
					}
					brickToScript++;
				}

				for (Brick brick : bricks) {
					tmpList.add(brick);
				}

				projectManager.getCurrentSprite().addScript(sId + 1, newScript);

				for (int j = brickToScript; j < tmpList.size(); j++) {
					Brick brickToCopy = tmpList.get(j);
					projectManager.getCurrentSprite().getScript(sId + 1).addBrick(brickToCopy);
				}

				for (int i = bricks.size(); i > brickToScript; i--) {
					projectManager.getCurrentSprite().getScript(sId).removeBrick(bricks.get(bricks.size() - 1));
				}
			}
		} else if (draggedBrick instanceof WhenStartedBrick) {

			int sId = getScriptId(to);
			ProjectManager projectManager = ProjectManager.getInstance();
			Script newScript = new StartScript(projectManager.getCurrentSprite());
			if (toLastScript) {
				projectManager.getCurrentSprite().addScript(newScript);
			} else {
				ArrayList<Brick> bricks = projectManager.getCurrentSprite().getScript(sId).getBrickList();

				ArrayList<Brick> tmpList = new ArrayList<Brick>();

				int brickToScript = 0;
				for (Brick brick : bricks) {

					if (brick instanceof WhenStartedBrick) {
						projectManager.getCurrentSprite().getScript(sId).removeBrick(brick);
						break;
					}
					brickToScript++;
				}

				for (Brick brick : bricks) {
					tmpList.add(brick);
				}

				projectManager.getCurrentSprite().addScript(sId + 1, newScript);

				for (int j = brickToScript; j < tmpList.size(); j++) {
					Brick brickToCopy = tmpList.get(j);
					projectManager.getCurrentSprite().getScript(sId + 1).addBrick(brickToCopy);
				}

				for (int i = bricks.size(); i > brickToScript; i--) {
					projectManager.getCurrentSprite().getScript(sId).removeBrick(bricks.get(bricks.size() - 1));
				}
			}
			//BroadcastScript
		} else if (draggedBrick instanceof BroadcastReceiverBrick) {

			int sId = getScriptId(to);
			ProjectManager projectManager = ProjectManager.getInstance();
			Script newScript = new BroadcastScript(projectManager.getCurrentSprite());
			if (toLastScript) {
				projectManager.getCurrentSprite().addScript(newScript);
			} else {
				ArrayList<Brick> bricks = projectManager.getCurrentSprite().getScript(sId).getBrickList();

				ArrayList<Brick> tmpList = new ArrayList<Brick>();

				int brickToScript = 0;
				for (Brick brick : bricks) {

					if (brick instanceof BroadcastReceiverBrick) {
						projectManager.getCurrentSprite().getScript(sId).removeBrick(brick);
						break;
					}
					brickToScript++;
				}

				for (Brick brick : bricks) {
					tmpList.add(brick);
				}

				projectManager.getCurrentSprite().addScript(sId + 1, newScript);

				for (int j = brickToScript; j < tmpList.size(); j++) {
					Brick brickToCopy = tmpList.get(j);
					projectManager.getCurrentSprite().getScript(sId + 1).addBrick(brickToCopy);
				}

				for (int i = bricks.size(); i > brickToScript; i--) {
					projectManager.getCurrentSprite().getScript(sId).removeBrick(bricks.get(bricks.size() - 1));
				}
			}
		} else if (draggedBrick instanceof LoopBeginBrick) {
			Log.d("TESTING", "Setting Loop Begin/end Brick");
			if (insertLoop) {
				LoopEndBrick loopEndBrick = new LoopEndBrick(ProjectManager.getInstance().getCurrentSprite(),
						(LoopBeginBrick) draggedBrick);

				int sId = getScriptId(to);
				//				ArrayList<Brick> bricks = ProjectManager.getInstance().getCurrentSprite().getScript(sId).getBrickList();
				int bId = ProjectManager.getInstance().getCurrentSprite().getScript(sId).getBrickList()
						.indexOf(draggedBrick) + 1;

				//				for (int i = 0; i < bricks.size(); i++) {
				//					if (bricks.get(i) instanceof LoopBeginBrick) {
				//						bId = i + 1;
				//					}
				//				}

				ProjectManager.getInstance().getCurrentSprite().getScript(sId).addBrick(bId, loopEndBrick);

				((LoopBeginBrick) draggedBrick).setLoopEndBrick(loopEndBrick);
				insertLoop = false;
			}
		} else {

			if (toLastScript) {
				int sId = getScriptId(fromTest);
				int toSCript = sprite.getNumberOfScripts() - 1;
				Log.d("TESTING", "sID " + sId);
				sprite.getScript(sId).removeBrick(draggedBrick);
				sprite.getScript(toSCript).addBrick(draggedBrick);
			}
		}

		draggedBrick = null;
		clearScriptBricks();
		notifyDataSetChanged();
	}

	private int clearScriptBricks() {

		ProjectManager projectManager = ProjectManager.getInstance();
		int nrScripts = projectManager.getCurrentSprite().getNumberOfScripts();

		for (int i = 0; i < nrScripts; i++) {

			Script tmpScript = projectManager.getCurrentSprite().getScript(i);

			if (tmpScript.containsBrickOfType(BroadcastReceiverBrick.class) == true) {

				int brickIndex = tmpScript.containsBrickOfTypeReturnsFirstIndex(BroadcastReceiverBrick.class);
				tmpScript.removeBrick(tmpScript.getBrick(brickIndex));
			}

			if (tmpScript.containsBrickOfType(WhenStartedBrick.class) == true) {

				int brickIndex = tmpScript.containsBrickOfTypeReturnsFirstIndex(WhenStartedBrick.class);
				tmpScript.removeBrick(tmpScript.getBrick(brickIndex));
			}

			if (tmpScript.containsBrickOfType(WhenBrick.class) == true) {

				int brickIndex = tmpScript.containsBrickOfTypeReturnsFirstIndex(WhenBrick.class);
				tmpScript.removeBrick(tmpScript.getBrick(brickIndex));
			}

		}

		return 0;
	}

	public void remove(int index) {

		if (index < 0) {
			index = getBrickPosition();
		}

		if (index < currentScriptPosition) {
			currentScriptPosition--;
		}

		if (draggedBrick instanceof LoopBeginBrick) {
			Log.d("TESTING", "BeginBrick index: " + getScriptId(index));
			LoopBeginBrick loopBeginBrick = (LoopBeginBrick) draggedBrick;
			sprite.getScript(getScriptId(index)).removeBrick(loopBeginBrick.getLoopEndBrick());

		} else if (draggedBrick instanceof LoopEndBrick) {
			Log.d("TESTING", "EndBrick index: " + getScriptId(index));
			LoopEndBrick loopEndBrick = (LoopEndBrick) draggedBrick;
			sprite.getScript(getScriptId(index)).removeBrick(loopEndBrick.getLoopBeginBrick());
		}

		sprite.getScript(getScriptId(index)).removeBrick(draggedBrick);
		Log.d("TESTING", "Remove");

		draggedBrick = null;

		notifyDataSetChanged();
	}

	public OnLongClickListener getOnLongClickListener() {
		return longClickListener;
	}

	public int getCount() {

		int count = 0;
		for (int i = 0; i < sprite.getScriptCount(); i++) {
			count += getBrickCount(i) + 1;
		}
		return count;
	}

	public Object getItem(int element) {

		int count = 0;
		while (element > getBrickCount(count)) {
			element -= getBrickCount(count) + 1;
			count++;
		}
		if (element == 0) {
			return sprite.getScript(count);
		} else {
			return sprite.getScript(count).getBrick(element - 1);
		}
	}

	public int getBrickCount(int scriptIndex) {
		return sprite.getScript(scriptIndex).getBrickList().size();
	}

	public long getItemId(int index) {
		return index;
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		if (getItem(position) instanceof Brick) {
			View currentBrickView;

			if (getItem(position) instanceof WhenBrick) {
				WhenBrick brick = (WhenBrick) getItem(position);
				currentBrickView = brick.getPrototypeView(context);
			} else if (getItem(position) instanceof BroadcastReceiverBrick) {
				BroadcastReceiverBrick brick = (BroadcastReceiverBrick) getItem(position);
				currentBrickView = brick.getPrototypeView(context);
			} else {
				Brick brick = (Brick) getItem(position);
				currentBrickView = brick.getView(context, position, this);
			}

			if (draggedBrick != null && dragTargetPosition == position) {
				return insertionView;
			}

			// Hack!!!
			// if wrapper isn't used the longClick event won't be triggered
			//ViewGroup wrapper = (ViewGroup) View.inflate(context, R.layout.construction_brick_wrapper, null);
			ViewGroup wrapper = (ViewGroup) View.inflate(context, R.layout.brick_wrapper, null);
			if (currentBrickView.getParent() != null) {
				((ViewGroup) currentBrickView.getParent()).removeView(currentBrickView);
			}

			if (draggedBrick != null && dragTargetPosition == 0) {
				return null;
			}

			wrapper.addView(currentBrickView);
			wrapper.setOnLongClickListener(longClickListener);

			if (position == pos) {
				if (insertedBrick) {
					insertedBrick = false;
					DragAndDropListView listView = (DragAndDropListView) ((ScriptActivity) context)
							.findViewById(R.id.brick_list_view);
					listView.onLongClick(currentBrickView);
					return insertionView;
				}
			}

			return wrapper;

		} else {

			View view = null;

			if (getItem(position) instanceof BroadcastScript) {
				view = new BroadcastReceiverBrick(sprite, (BroadcastScript) getItem(position)).getView(context,
						position, this);
			} else if (getItem(position) instanceof StartScript) {
				view = new WhenStartedBrick(sprite, (Script) getItem(position)).getView(context, position, this);
			} else if (getItem(position) instanceof WhenScript) {
				view = new WhenBrick(sprite, (WhenScript) getItem(position)).getView(context, position, this);
			}
			return view;
		}
	}

	public int getScriptId(int index) {
		int count = 0;
		while (index > getBrickCount(count)) {

			index -= getBrickCount(count) + 1;
			count++;
		}
		return count;
	}

	private boolean isBrick(int index) {
		Object obj = getItem(index);
		if (obj instanceof Brick) {
			return true;
		} else {
			return false;
		}
	}

	private int getScriptPosition(int index, int script) {

		int scriptCount = 0;

		while (scriptCount < script) {
			index -= sprite.getScript(scriptCount).getBrickList().size() + 1;
			scriptCount++;
		}

		return --index;
	}

	public int getScriptCount() {
		return sprite.getScriptCount();
	}

	public void setTouchedScript(int index) {
		if (!(index == currentScriptPosition)) {
			if (!(index < 0)) {
				if (!isBrick(index)) {
					if (draggedBrick == null) {
						ProjectManager.getInstance().setCurrentScript(sprite.getScript(getScriptId(index)));
						setCurrentScriptPosition(index);
						//						notifyDataSetChanged();
					}
				}
			}
		}
	}

	public void setCurrentScriptPosition(int position) {
		currentScriptPosition = position;
	}

	public int getSpriteSize() {
		return sprite.getScriptCount();
	}

	private int getBrickPosition() {
		int brickCount = 0;

		for (int count = 0; count < sprite.getScriptCount(); count++) {
			brickCount += sprite.getScript(count).getBrickList().size() + 1;
		}

		return brickCount - 1;
	}

	// Just for Testing
	public int getChildCountFromLastGroup() {
		return getBrickCount(getScriptCount() - 1);
	}

	public int getGroupCount() {
		return getScriptCount();
	}

	public Brick getChild(int groupPosition, int childPosition) {

		return (Brick) getItem(getScriptId(groupPosition) + (childPosition + 1));
	}

	public int rearangeBricks(int pos) {

		//		int orig = pos;
		Script script = ProjectManager.getInstance().getCurrentScript();
		Brick brick = script.getBrick(script.getBrickList().size() - 1);
		ProjectManager.getInstance().getCurrentScript().removeBrick(brick);

		int sId = getScriptId(pos);

		if (sId == 0 && pos > 0) {
			pos--;
		}

		for (int i = 0; i < sId; i++) {
			pos -= (sprite.getScript(i).getBrickList().size() + 1);
		}

		//		if (sprite.getScriptCount() > 1) {
		//			if (!isBrick(orig)) {
		//				orig++;
		//				if (sId != getScriptId(orig)) {
		//					sId = getScriptId(orig);
		//					pos -= (sprite.getScript(sId - 1).getBrickList().size() + 1);
		//				}
		//			}
		//		}

		//		Log.d("TESTING", "Adding in Script: " + sId + ", pos: " + pos + ", orig: " + orig);
		sprite.getScript(sId).addBrick(pos, brick);

		int newPos = 0;
		for (int i = 0; i < sId; i++) {
			newPos += (sprite.getScript(i).getBrickList().size() + 1);
		}
		newPos += pos + 1;

		notifyDataSetChanged();

		return newPos;
	}

	public void setInsertedBrickpos(int Npos) {
		insertedBrick = true;
		pos = Npos;
	}

}
