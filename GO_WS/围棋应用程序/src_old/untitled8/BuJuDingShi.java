package untitled8;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class BuJuDingShi extends GoBoardLian1{
   public BuJuDingShi() {
   }
   //第一步决定xuan(旋转)和fuan(翻转)参数，fuan可能无法一步实现。

   //从第二步开始计算与周边最近点的距离,距离在三件夹或挂的范围内，
   //视为定式程度的关联。如果只涉及一个角，存储为定式；如果涉及两个或
   //两个以上的角，即确认为布局定式（之间的选择和要点更多，更复杂）。
   //内部可能包含许多小定式，但是与周围环境的相关性更强。

   //角部计数：jbjs；等于0则布局（更确切的说是占角结束）结束，初始值为4；

   //2003年12月31页：两个角在定式中相关。角部两手视为稳妥，局部可以告一段落
   //。实在的占领：要看两个方向是否都有紧逼，本身的结构是否合理。22开始
   //脱先，评价为角部和边上稳定，中间白有一块待定。

   //在前述布局定式中，白十和黑十一的交换，视为两处的定式，各自脱先，即
   //小脱先。脱先分为以下几种：
   //a.彻底脱先，局部已经不急或者已经定型，此时脱先走它处；
   //b.仍保留在此处进行，但是在别处进行先手利用，为本处服务；
   //c.找劫材视为情况b，或者还是独立为好，因为可能连走两手消劫。
   //d.有时在一个整体中也存在内部的脱先情况，称为小脱先，也要分阿，a,b,c


}
