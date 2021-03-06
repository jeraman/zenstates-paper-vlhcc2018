

import controlP5.*;
import processing.core.PApplet;



class SetBBRandomTask extends SetBBTask {
  private Object delay;
  //private timestamp;

  public SetBBRandomTask (PApplet p, ControlP5 cp5) {
    super(p, cp5, ("rand_" + (int)p.random(0, 100)), new Expression("math.random()"));

    update_delay("0");
  }

  
  SetBBRandomTask clone_it() {
	  SetBBRandomTask clone = new SetBBRandomTask(this.p, this.cp5);
	  
	  clone.variableName	= this.variableName;
	  clone.delay 			= this.delay;
	  clone.value 			= this.value;
	  clone.timerMilestone 	= this.timerMilestone;
	  clone.timer          	= this.timer;
	  clone.repeat			= this.repeat;
	  
	  return clone;
  }

  void update_delay(String v) {
    this.delay = new Expression(v);
    //update_content();
  }

  void run() {
    if (!should_run()) return;

    String delay_val = (evaluate_value(this.delay)).toString();
    
    //String rootTimer = "$" + ((ZenStates)p).canvas.root.get_formated_blackboard_title() + "_timer";
    
    //Expression ne = new Expression("("+rootTimer+"%"+delay_val+")>("+delay_val+"-0.05)");
    
    boolean reached_delay = true;
    
    if (!delay_val.equals("0")) {
    	Expression ne = new Expression("("+timer+"%"+delay_val+")>("+delay_val+"-0.05)");
    	reached_delay = (boolean)(evaluate_value(ne));
    }

    //p.println("exp: " + ne.toString() + "   reached_delay? " + reached_delay);
    //p.println("reached_delay " + reached_delay);
    this.status = Status.RUNNING;
    
    if(reached_delay) super.run();
    
    this.status = Status.DONE;
  }


  Group load_gui_elements(State s) {
	  /*
    CallbackListener cb_enter = generate_callback_enter();
    //CallbackListener cb_leave = generate_callback_leave();

    //this.set_gui_id(s.get_name() + " " + this.get_name());
    String g_name = this.get_gui_id();
    
    String textlabel = "Random variable";
    int font_size 	 = (int)(((ZenStates)p).get_font_size());
    int textwidth 	 = (int)((ZenStates)p).textWidth(textlabel);
    int backgroundheight = (int)(font_size* 10.5);

    //ControlP5 cp5 = HFSMPrototype.instance().cp5();

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
	  
	  textlabel 	 			= "Random variable";
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

    cp5.addTextfield(g_name+ "/delay")
      .setPosition(localx, localy+(1*localoffset))
      .setSize(w, (int)(font_size*1.25))
      .setGroup(g)
      .setAutoClear(false)
      .setLabel("delay")
      .setText(this.delay.toString())
      .align(ControlP5.CENTER, ControlP5.CENTER,ControlP5.CENTER, ControlP5.CENTER)
      .onChange(cb_enter)
      .onReleaseOutside(cb_enter)
      .getCaptionLabel().align(ControlP5.CENTER, ControlP5.BOTTOM_OUTSIDE)
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

            if (s.equals(get_gui_id() + "/name")) {
                String text = theEvent.getController().getValueLabel().getText();
                //if the name is empty, resets
                if (text.trim().equalsIgnoreCase("")) {
                  ((Textfield)cp5.get(get_gui_id() + "/name")).setText(name);
                  return;
                }
                update_variable_name(text);
                //System.out.println(s + " " + text);
            }

            if (s.equals(get_gui_id() + "/delay")) {
                String nv = theEvent.getController().getValueLabel().getText();
                if (nv.trim().equals("")) {
                  nv="1";
                  ((Textfield)cp5.get(get_gui_id()+ "/delay")).setText(nv);
                }
                update_delay(nv);
            }

            check_repeat_toggle(s, theEvent);
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
	  nv = ((Textfield)cp5.get(g_name+"/delay")).getText();
	  update_delay(nv);

  }

}
