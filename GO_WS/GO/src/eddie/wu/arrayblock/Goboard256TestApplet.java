/*
 * Created on 2005-5-14
 *


 */
package eddie.wu.arrayblock;

import java.awt.Color;
import java.awt.Event;

import org.apache.log4j.Logger
;

import eddie.wu.manual.LoadGMDGoManual;

/**
 * @author eddie
 *
 * TODO To change the template for this generated type comment go to

 */
public class Goboard256TestApplet 

extends GoBoard256Applet {
	private static final Logger log = Logger.getLogger(Goboard256TestApplet.class);
	int shoushu = 0;

	int count = 0;

	byte[] temp = new LoadGMDGoManual("../doc/围棋打谱软件").loadOneFromAllGoManual(1,453);

	public void init() {
		this.setBackground(Color.ORANGE);
		work = this.createImage(560, 560);
		if (work == null) {
			log.debug("work==null");
		} else {
			g = work.getGraphics();
			log.debug("work!=null");
		}
		//Constant.DEBUG_CGCL = false;
		for (int i = 1; i <= 229; i++) {
			shoushu++;
			log.debug("shoushu=" + shoushu);
			log.debug("a=" + temp[count] + ",b=" + temp[count + 1]);
			goBoard.cgcl(temp[count++], temp[count++]);

		}
	}
	public boolean mouseDown(Event e, int x, int y) { //接受鼠标输入
	    //if(KEXIA==true){
	    //KEXIA=false;//只有机器完成一手,才能继续.
	    log.debug("方法 mousedown");
	    if(count>=temp.length) return true;
	    byte a = (byte) ( (x - 4) / 28 + 1); //完成数气提子等.
	    byte b = (byte) ( (y - 4) / 28 + 1);
	    a=temp[count++];
	    b=temp[count++];
	    shoushu++;
	    goBoard.cgcl( a,b);
//	    if (Constant.\== true) {
//	      goBoard.output();
//	    }
	    repaint();
	    log.debug("方法 mousedown");
	    return true; //向容器传播,由Frame处理
	    //}
	    // else  return true;
	  }
}
