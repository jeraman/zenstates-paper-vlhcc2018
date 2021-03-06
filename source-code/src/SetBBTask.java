

import controlP5.*;
import processing.core.PApplet;


class SetBBTask extends Task {

	Object value;
	String variableName;
	float timer;
	float timerMilestone;
	

	public SetBBTask (PApplet p, ControlP5 cp5, String taskname, Object value) {
		super(p, cp5, taskname);
		this.variableName = taskname;
		this.value = value;
		this.timerMilestone = 0;
		this.timer          = 0;
	}
	
	public SetBBTask (PApplet p, ControlP5 cp5, String taskname, Object value, boolean repeat) {
		this(p, cp5, taskname, value);
		this.repeat 		= repeat;
	}
	

	/*
  public SetBBTask (PApplet p, String taskname, Object value) {
    super(p, taskname);
    this.value = value;
  }
	 */

	void build(PApplet p, ControlP5 cp5) {
		this.p = p;
		this.cp5 = cp5;
	}

	SetBBTask clone_it() {
		//if (this instanceof )
		return new SetBBTask(this.p, this.cp5, this.name, this.value, this.repeat);
	}
	
	//updates the stateTimer variable related to this state machine
	void update_timer() {
		this.timer = ((float)p.millis()/1000f)-timerMilestone;
	}
	
		//resets the timer variable related to this setBBTask
	void reset_timer() {
		this.timerMilestone = (float)p.millis()/1000f;
		this.timer          = 0;
	}
	
	//special "should run" modifed for blackboard variables involving timers
	boolean should_run() {
		if (first_time) reset_timer();
		boolean should_run = super.should_run();
		//if (should_run) update_timer();
		update_timer();
		return should_run;
	}


	void run() {
		if (!should_run()) return;
		
		Blackboard board = ZenStates.instance().board();
		this.status = Status.RUNNING;
		board.put(variableName, evaluate_value(value));
		this.status = Status.DONE;
	}

	void stop() {
		super.stop();
		this.status = Status.INACTIVE;
	}

	void update_value(Object new_value) {
		value = new_value;
	}

	void update_variable_name(String newname) {
		this.variableName = newname;
	}

	void update_status() {
	}



	Group load_gui_elements(State s) {
		/*
		//PApplet p = HFSMPrototype.instance();
		CallbackListener cb_enter = generate_callback_enter();
		//CallbackListener cb_leave = generate_callback_leave();
		//ControlP5 cp5 = HFSMPrototype.instance().cp5();
		int c1 = p.color(255, 50);
		int c2 = p.color(255, 25);

		//this.set_gui_id(s.get_name() + " " + this.get_name());
		String g_name = this.get_gui_id();
		
	    String textlabel = "Blackboard variable";
	    int font_size 	 = (int)(((ZenStates)p).get_font_size());
	    int textwidth 	 = (int)((ZenStates)p).textWidth(textlabel);
	    int backgroundheight = (int)(font_size* 10.5);

	    Group g = cp5.addGroup(g_name)
	    	    //.setPosition(x, y) //change that?
	    	    .setHeight(font_size)
	    	    .setWidth((10*((ZenStates)p).FONT_SIZE))
	    	    .setBackgroundHeight(backgroundheight)
	    	    .setColorBackground(p.color(255, 50)) //color of the task
	    	    .setBackgroundColor(p.color(255, 25)) //color of task when openned
	    	    .setLabel(textlabel)
	    	    ;

		g.getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER);

		 int localx = 10, localy = (int)(font_size), localoffset = 3*font_size;
		 int w = g.getWidth()-(localx*2);
		 */
		
		 Group g					= super.load_gui_elements(s);
		  CallbackListener cb_enter = generate_callback_enter();
		  String g_name			  	= this.get_gui_id();
		  int w 					= g.getWidth()-(localx*2);
		  
		  textlabel 	 			= "Blackboard variable";
		  backgroundheight 			= (int)(font_size* 10.5);
		    
		  g.setBackgroundHeight(backgroundheight);
		  g.setLabel(textlabel);

		cp5.addTextfield(g_name+ "/name")
		.setPosition(localx, localy)
		.setSize(w, (int)(font_size*1.25))
		.setGroup(g)
		.setAutoClear(false)
		.setLabel("name")
		.setText(this.variableName)
		.onChange(cb_enter)
		.onReleaseOutside(cb_enter)
		.getCaptionLabel().align(ControlP5.CENTER, ControlP5.BOTTOM_OUTSIDE);
		;

		cp5.addTextfield(g_name+ "/value")
		.setPosition(localx, localy+localoffset)
		.setSize(w, (int)(font_size*1.25))
		.setGroup(g)
		.setAutoClear(false)
		.setLabel("value")
		.setText(this.value.toString())
		.onChange(cb_enter)
		.onReleaseOutside(cb_enter)
		.getCaptionLabel().align(ControlP5.CENTER, ControlP5.BOTTOM_OUTSIDE);
		;

		create_gui_toggle(localx, localy+(2*localoffset), w, g, cb_enter);

		return g;
	}

	CallbackListener generate_callback_enter() {
		return new CallbackListener() {
			public void controlEvent(CallbackEvent theEvent) {

				//if this group is not open, returns...
				if (!((Group)cp5.get(get_gui_id())).isOpen()) return;

				String s = theEvent.getController().getName();
				//println(s + " was entered");

				if (s.equals(get_gui_id() + "/name")) {
					String text = theEvent.getController().getValueLabel().getText();
					//if the name is empty, resets
					if (text.trim().equalsIgnoreCase("")) {
						((Textfield)cp5.get(get_gui_id() + "/name")).setText(name);
						return;
					}
					update_variable_name(text);
					System.out.println(s + " " + text);
				}
				if (s.equals(get_gui_id() + "/value")) {
					String newvalue = theEvent.getController().getValueLabel().getText();
					//if the name is empty, resets
					if (newvalue.trim().equalsIgnoreCase("")) {
						((Textfield)cp5.get(get_gui_id() + "/value")).setText(value.toString());
						return;
					}
					update_value(new Expression(newvalue));
					System.out.println(s + " " + newvalue);
				}

				check_repeat_toggle(s, theEvent);
			}
		};
	}

	CallbackListener generate_callback_leave() {
		return new CallbackListener() {
			public void controlEvent(CallbackEvent theEvent) {

				String s = theEvent.getController().getName();

				String newtext = theEvent.getController().getValueLabel().getText();
				String oldtext = "";

				if (s.equals(get_gui_id() + "/name"))
					oldtext = variableName;
				else if (s.equals(get_gui_id() + "/value"))
					oldtext = value.toString();
				else  return;

				//if the user tried to change but did not press enter
				if (!newtext.replace(" ", "").equals(oldtext)) {
					//resets the test for the original
					//ControlP5 cp5 = HFSMPrototype.instance().cp5();
					Textfield t = (Textfield)cp5.get(s);
					t.setText(oldtext);
				}
			}
		};
	}

	void reset_gui_fields() {
		String g_name = this.get_gui_id();
		String nv;

		//if this group is not open, returns...
		if (!((Group)cp5.get(get_gui_id())).isOpen()) return;

		nv = ((Textfield)cp5.get(g_name+"/name")).getText();
		update_variable_name(nv);
		nv = ((Textfield)cp5.get(g_name+"/value")).getText();
		update_value(nv);

	}

}
