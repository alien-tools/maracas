package mainclient.classNowFinal;

import main.classNowFinal.ClassNowFinal;
import main.classNowFinal.ClassNowFinalAbs;

public class ClassNowFinalAnonymousSub {
	public void classNowFinalAnonymousSub() {
		ClassNowFinal c = new ClassNowFinal() {
		};
	}

	public void classNowFinalAnonymousSubAbs() {
		ClassNowFinalAbs c = new ClassNowFinalAbs() {
			@Override
			public int m() {
				return 0;
			}
		};
	}
}
