package eddie.wu.domain.analy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import eddie.wu.domain.BlankBlock;
import eddie.wu.domain.Block;
import eddie.wu.domain.BoardColorState;
import eddie.wu.domain.BoardPoint;
import eddie.wu.domain.Constant;
import eddie.wu.domain.NeighborState;
import eddie.wu.domain.Point;
import eddie.wu.domain.Step;
import eddie.wu.domain.SymmetryResult;
import eddie.wu.search.global.Candidate;
import eddie.wu.search.global.CandidateComparator;

/**
 * 小棋盘 得到候选点
 * 
 * @author think
 *
 */
public class SmallGoBoard extends TerritoryAnalysis {
	private static final Logger log = Logger.getLogger(SmallGoBoard.class);

	public SmallGoBoard(BoardColorState colorState) {
		super(colorState);
	}

	public SmallGoBoard(byte[][] state) {
		super(BoardColorState.getInstance(state, Constant.BLACK));
	}

	public SmallGoBoard(byte[][] state, int whoseTurn) {
		super(BoardColorState.getInstance(state, whoseTurn));
	}

	/**
	 * 
	 * @param whoseTurn
	 * @param filterSymmetricEquivalent
	 * @return
	 */
	public List<Candidate> getCandidate(int whoseTurn,
			boolean filterSymmetricEquivalent) {
		return getCandidate(whoseTurn, filterSymmetricEquivalent, 0);
	}

	/**
	 * 1. reduce candidates by symmetry<br/>
	 * 2. sort by priority.<br/>
	 * 3. avoid fill eye point.<br/>
	 * 4. eating/capturing first <br/>
	 * 5. captured last (gift 送吃), PASS in between<br/>
	 * 5.1 送吃的子如果已经不活（无眼），优先于弃权；如果有眼位，则在弃权之后考虑<br/>
	 * 
	 * @return
	 */
	public List<Candidate> getCandidate(int color,
			boolean filterSymmetricEquivalent, int expectedScore) {
		assert this.boardSize <= 5;
		List<Candidate> fillingEyes = new ArrayList<Candidate>();
		List<Point> fillingEyeP = new ArrayList<Point>();
		// Map<Point, Integer> breathMap = new HashMap<Point, Integer>();

		Set<Point> points = new HashSet<Point>();
		for (int row = 1; row <= boardSize; row++) {
			for (int column = 1; column <= boardSize; column++) {
				BoardPoint boardPoint = getBoardPoint(row, column);
				if (boardPoint.getColor() == Constant.BLANK) {

					BlankBlock blankBlock = boardPoint.getBlankBlock();
					if (blankBlock.isInitBlankBlock()) {

					} else if (blankBlock.isEyeBlock()) {
						/**
						 * 3. avoid fill eye point. <br/>
						 * 防止自填眼位,尚未处理大眼位做眼的情况。
						 */
						int blocks = blankBlock.getNeighborBlocks().size();
						if (blankBlock.isSinglePointEye()) {
							// 己方不填眼，对方是否可下看气数。
							if ((color == Constant.BLACK && blankBlock
									.isBlackEye())
									|| (color == Constant.WHITE && blankBlock
											.isWhiteEye())) {

								/**
								 * need to handle this exceptional case, the eye
								 * might be an fake eye<br/>
								 * ## 01,02,03,04 <br/>
								 * 01[B, W, _, W]01<br/>
								 * 02[_, B, W, _]02<br/>
								 * 03[B, _, B, W]03<br/>
								 * 04[_, B, W, _]04<br/>
								 * ## 01,02,03,04 <br/>
								 */
								if (blocks == 1) {
									continue;// do not fill real eyes.
								} else {
									// may connect blocks
									boolean realSingleEye = this
											.isRealSingleEye(
													blankBlock
															.getMinBreathNeighborBlock(),
													blankBlock.getUniquePoint());
									if (realSingleEye) {
										continue;
									} else {
										/**
										 * Avoid Black to play at [3,3] in case
										 * below<br/>
										 * text[0] = new String("[_, W, _]");<br/>
										 * text[1] = new String("[B, W, B]");<br/>
										 * text[2] = new String("[B, B, _]");<br/>
										 */
										if (blankBlock
												.getMinBreathNeighborBlock()
												.getBreaths() >= 2) {
											// maybe we can handle it better now
											fillingEyeP.add(boardPoint
													.getPoint());
											// continue;
										}
									}
								}

							}
						} else {
							// bigger eye
						}
					} else { // not eye block

					}

					int breaths = breathAfterPlay(boardPoint.getPoint(), color)
							.size();
					if (breaths > 0) {
						points.add(boardPoint.getPoint());
					}
				}
			}
		}

		/**
		 * 提前识别出可能导致全局再现的候选点。
		 * 
		 */
		for (Iterator<Point> iter = points.iterator(); iter.hasNext();) {
			
			boolean duplicate = this.globalDuplicate(new Step(iter.next(),
					color));
			if (duplicate){
				iter.remove();
				//otherwise, normalized one maybe the one reach duplicated.
				filterSymmetricEquivalent = false;
			}
		}

		/**
		 * 处理本质上等价的候选棋步.在棋盘上的子有对称性时减少候选点的个数。
		 */
		List<Point> can = new ArrayList<Point>();

		if (filterSymmetricEquivalent == true) {
			SymmetryResult symmetryResult = this.getSymmetryResult();
			if (symmetryResult.getNumberOfSymmetry() != 0) {

				Set<Point> points2 = new HashSet<Point>();
				Set<Point> listAll = new HashSet<Point>();
				for (Iterator<Point> iter = points.iterator(); iter.hasNext();) {
					Point point = iter.next();
					if (listAll.contains(point))
						continue;

					List<Point> listVar = point.deNormalize(symmetryResult);
					listAll.addAll(listVar);
					// only keep one of all the symmetric candidates.
					points2.add(listVar.get(0));

				}
				can.addAll(points2);
			} else {
				can.addAll(points);
			}
		} else {
			can.addAll(points);
		}
		
		

		List<Candidate> gifts = new ArrayList<Candidate>();
		List<Candidate> decreaseBreath = new ArrayList<Candidate>();
		List<Candidate> eatingDeads = new ArrayList<Candidate>();
		/**
		 * decide sequence by priority.<br/>
		 * capture sequence. <br/>
		 */
		List<Candidate> candidates = new ArrayList<Candidate>();
		for (Point point : can) {
			Candidate candidate = new Candidate();
			candidate.setStep(new Step(point, color, getShoushu() + 1));
			if (fillingEyeP.contains(point)) {
				fillingEyes.add(candidate);
				continue;
			}

			NeighborState state = null;
			try {
				state = this.getNeighborState_forCandidate(point, color);
			} catch (RuntimeException e) {
				if (log.isEnabledFor(Level.WARN)) {
					log.warn(this.getBoardColorState().getStateString());
					log.warn("point" + point);

				}
				System.err.println(point);
				System.err.print(getBoardColorState().getStateString());
				// this.printState();
				throw e;
			}

			/**
			 * avoid eaten dead enemy to enhance live target.
			 */
			if (state.isEating()) {
				if (state.getFriendBlockNumber() == 1) {
					Block friendBlock = state.getFriendBlocks().iterator()
							.next();
					Point pointT = friendBlock.getBehalfPoint();
					if (friendBlock.getNumberOfPoint() >= 4) {
						boolean selfLive = false;
						if (boardSize > 5) {
							selfLive = this.isAlreadyLive_dynamic(pointT);
						} else {
							selfLive = this.isStaticLive(pointT);
						}
						if (selfLive) {
							Block enemyBlock = state.getEatenBlocks()
									.iterator().next();
							boolean enemyDead = this
									.isRemovable_static(enemyBlock);
							if (enemyDead) {
								Candidate eatingDead = new Candidate();
								eatingDead.setStep(new Step(point, color,
										getShoushu() + 1));
								eatingDead.setEatingDead(true);
								eatingDeads.add(eatingDead);
								continue;
							}
						}

					}
				}
			}
			candidate.setEating(state.isEating());
			candidate.setGifting(state.isGifting());
			candidate.setCapturing(state.isCapturing());
			candidate.setRemoveCapturing(state.getRemoveCapturing());
			candidate.setBreaths(state.getBreath());
			candidate.setIncreasedBreath(state.getIncreasedBreath());
			candidate.setAttacking(state.isAttacking());
			candidate.setConnection(state.getConnection());

			/**
			 * 缩小眼位的紧气看成类似送礼，送吃。<br/>
			 * TODO: further refinement.
			 */
			if (state.isGifting()) {
				if (state.getGift() == null) {

					gifts.add(candidate);
				} else if (state.getGift().getOriginalStones() == 0) {
					// 单子扑入.
					candidates.add(candidate);// gift one point usually good.
				} else if (state.getGift().getOriginalBreath() >= 2) {

					if (state.getGift().getOriginalStones() >= 6) {
						// should not gift so much.
					} else {
						gifts.add(candidate);
					}
				} else {
					candidates.add(candidate);
				}

			} else if (state.isEating() == false
					&& state.isCapturing() == false
					&& state.getIncreasedBreath() < 0) {
				// not favor the move cannot increase breath.
				decreaseBreath.add(candidate);
			} else {
				candidates.add(candidate);
			}

			// simple static eye counting
			if (state.isEating() == false) {
				candidate.setEyes(state.getEyes());
			}
		}

		// Collections.sort(can, new LowLineComparator());
		Collections.sort(candidates, new CandidateComparator());

		Candidate candidatePass = new Candidate();
		candidatePass.setStep(new Step(null, color, getShoushu() + 1));
		if (this.getStepHistory().getAllSteps().isEmpty() == false
				&& this.getLastStep().isPass() == true) {
			// 前一步对方弃权,下一步有限考虑弃权,有望及早到达终点状态.
			// this logic is only good for 2*2 board.
			if (boardSize <= 4) {
				candidates.add(0, candidatePass);
			}
		} else {
			candidates.add(candidatePass);
		}
		// 送礼点也可能是正解，但可能性较小，排在弃权后面。
		Collections.sort(gifts, new CandidateComparator());
		candidates.addAll(gifts);
		// put eating dead at the end, so for small board, only depends on
		// static live/dead judgment.
		candidates.addAll(decreaseBreath);
		candidates.addAll(eatingDeads);
		candidates.addAll(fillingEyes);
		return candidates;
	}
}
