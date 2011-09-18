/*
 * Created on 2005-4-21
 *
 * Eddie Wu CopyRight @2005
 */
package eddie.wu.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eddie.wu.linkedblock.ColorUtil;

/**
 * some concern; because the blank/white block is different from blank point
 * block. and we use same type block to present them, so must decide whether to
 * invoke special method according to the color.
 * 
 * 棋块--breath only retain in the block. the breath of the point in the block is
 * nonsense. 1. block is divide into three subtype; one is blank point block(and
 * breath block is special. surround only by same color block) another is black
 * block the last is white block
 * 
 * 2. black/white block has enemy Block. blank point block has no enemy Block.
 * 3. black/white block has breath Block. blank point block has no breath Block.
 * 4. black/white block has breath point set. blank point block has no set of
 * breath point
 * 
 * fix 1; decide to contain point. not BoardPoint. avoid directional reference.
 * 
 * @author eddie
 */
public class Block extends BasicBlock implements Cloneable,
		java.io.Serializable {
	Log log = LogFactory.getLog(Block.class);

	/* only used by blank/white block */
	private Set<Point> allBreathPoints = new HashSet<Point>();// 棋块气点集合
	private boolean breathCalculated;

	// 不入气点的集合。死活计算用，自行初始化。
	private Set<Point> eyes = new HashSet<Point>();
	// 不入气点的集合,单个不能活棋，属于假眼。死活计算用，自行初始化。
	private Set<Point> fakeEyes = new HashSet<Point>();

	private Set<Block> enemyBlocks = new HashSet<Block>();

	private Set<Block> breathBlocks = new HashSet<Block>();

	public void changeBlockForEnemyBlock(Block newBlock) {
		Block block = null;
		for (Iterator iter = enemyBlocks.iterator(); iter.hasNext();) {
			block = (Block) iter.next();
			block.removeEnemyBlock(this);
			block.addEnemyBlock(newBlock);

		}

	}

	public void initAfterChangeToBlankblock() {
		allBreathPoints.clear();// 棋块气点集合
		enemyBlocks.clear();
		breathBlocks.clear();

	}

	public Block() {

	}

	public Block(byte color) {
		super(color);
	}

	public void addEye(Point Point) {
		eyes.add(Point);
	}

	public Set<Point> getEyes() {
		return eyes;
	}

	public int numberOfEyes() {
		return eyes.size();
	}

	public void removeEye(Point Point) {
		fakeEyes.add(Point);
		eyes.remove(Point);
	}

	public void addEye(Set<Point> eye) {
		eyes.addAll(eye);
	}

	public void addBreathPoint(Point point) {
		if (point.isNotValid()) {
			throw new RuntimeException("point is not valid when add Breath"
					+ point);
		}
		if (allBreathPoints.add(point)) {

		} else {// bug fix. it is possible to return false; because of shared
				// breath.
				// throw new
				// RuntimeException("failed when remove Breath Point"+point);
			log.info("return false when add Breath Point:" + point);
		}
	}

	public void removeBreathPoint(Point point) {
		if (point.isNotValid()) {
			throw new RuntimeException(
					"point is not valid when remove Breath Point" + point);
		}
		if (allBreathPoints.remove(point)) {

		} else {// bug fix. it is possible to return false; because of shared
				// breath.
				// throw new
				// RuntimeException("failed when remove Breath Point"+point);
			log.info("return false when remove Breath Point:" + point);
		}
	}

	// a special point on behalf the whole bolck.
	// byte row;
	// byte column;

	// public Block(byte row, byte column){
	// super();
	// this.row=row;
	// this.column=column;
	// color=Constant.BLANK_POINT;
	// }

	public short getBreaths() {
		return (short) this.allBreathPoints.size();
	}

	public void addEnemyBlock(Block block) {
		this.enemyBlocks.add(block);
	}

	public void removeEnemyBlock(Block block) {
		this.enemyBlocks.remove(block);
	}

	public String toString() {
		StringBuffer temp = new StringBuffer("\r\n[color=");
		List<Point> list = new ArrayList<Point>(allPoints.size());
		list.addAll(allPoints);
		Collections.sort(list,new RowColumnComparator());
		if (color == ColorUtil.BLACK || color == ColorUtil.WHITE) {
			temp.append(color);
			temp.append(", points=" + this.getTotalNumberOfPoint());
			temp.append(", breaths = " + this.getBreaths());
			
			temp.append(",\r\nallPoints=");			
			temp.append(list);			
			temp.append(",\r\nallBreathPoints=");
			//temp.append(allBreathPoints);
			list = new ArrayList<Point>(allBreathPoints.size());
			list.addAll(allBreathPoints);
			Collections.sort(list,new RowColumnComparator());
			temp.append(list);

			if (this.breathBlocks.isEmpty() == false) {
				temp.append(",\r\nbreathBlocks=[");
				for(Block tempBlock : this.breathBlocks){				
					temp.append(tempBlock.getTopLeftPoint()+ ", ");
				}
				temp.append("]");
			}
			if (this.enemyBlocks.isEmpty() == false) {
				temp.append(",\r\nenemyBlocks=[");
				for(Block tempBlock : this.enemyBlocks){				
					temp.append(tempBlock.getTopLeftPoint()+ ", ");
				}
				temp.append("]");
			}
		} else {
			temp.append(color);
			temp.append(",\r\nallPoints=");
			//temp.append(allPoints);
			temp.append(list);
		}

		/*
		 * bug fix 2
		 */
		// cause recursive invocation and get Stack Over Flow Error!
		// temp.append(",enemyBlocks=");
		// temp.append(this.enemyBlocks);
		temp.append("]");
		return temp.toString();
	}

	public boolean equals(Object o) {
		if (o instanceof Block) {
			Block other = (Block) o;
			if (other.getColor() == this.getColor()
					&& this.getAllPoints().equals(other.getAllPoints())
					&& this.getAllBreathPoints().equals(
							other.getAllBreathPoints())) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	public int hashCode() {
		return this.allPoints.hashCode() + this.allBreathPoints.hashCode() * 17;
	}

	/**
     *  
     */
	public void changeColorToBlank() {
		// TODO Auto-generated method stub
		allBreathPoints.clear();
		color = ColorUtil.BLANK_POINT;
		enemyBlocks = null;
		breathBlocks = null;
	}

	/**
	 * @return Returns the allBreathPoints.
	 */
	public Set<Point> getAllBreathPoints() {
		return allBreathPoints;
	}

	/**
	 * @param allBreathPoints
	 *            The allBreathPoints to set.
	 */
	public void setAllBreathPoints(Set<Point> allBreathPoints) {
		this.allBreathPoints = allBreathPoints;
	}

	/**
	 * @return Returns the breathBlock.
	 */
	public Set<Block> getBreathBlocks() {
		return breathBlocks;
	}

	public void addBreathBlock(Block abreathBlock) {
		breathBlocks.add(abreathBlock);
	}

	/**
	 * @param breathBlock
	 *            The breathBlock to set.
	 */
	public void setBreathBlocks(Set<Block> breathBlock) {
		this.breathBlocks = breathBlock;
	}

	/**
	 * @return Returns the enemyBlock.
	 */
	public Set<Block> getEnemyBlocks() {
		return enemyBlocks;
	}

	/**
	 * @param enemyBlock
	 *            The enemyBlock to set.
	 */
	public void setEnemyBlocks(Set<Block> enemyBlock) {
		this.enemyBlocks = enemyBlock;
	}

	public Object clone() {
		Block temp = null;
		try {
			temp = (Block) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		temp.allBreathPoints = (Set<Point>) ((HashSet<Point>) allBreathPoints)
				.clone();
		temp.allPoints = (Set) ((HashSet) allPoints).clone();
		temp.enemyBlocks = (Set) ((HashSet) enemyBlocks).clone();
		return temp;
	}

	public Shape getShape() {
		int minX = 20, minY = 20, maxX = 0, maxY = 0;
		for (Point point : getAllPoints()) {
			if (point.getRow() < minX)
				minX = point.getRow();

			if (point.getRow() > maxX)
				maxX = point.getRow();

			if (point.getColumn() < minY)
				minY = point.getColumn();
			if (point.getColumn() < maxY)
				maxY = point.getColumn();
		}
		Shape shape = new Shape();
		shape.setMaxX(maxX);
		shape.setMaxY(maxY);
		shape.setMinX(minX);
		shape.setMinY(minY);

		return shape;

	}

	public boolean isBreathCalculated() {
		return breathCalculated;
	}

	public void setBreathCalculated(boolean breathCalculated) {
		this.breathCalculated = breathCalculated;
	}

}
