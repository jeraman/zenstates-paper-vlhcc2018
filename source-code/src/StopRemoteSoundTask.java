

import controlP5.*;
import processing.core.PApplet;

////////////////////////////////////////
//implementing a task for OSC messages
public class StopRemoteSoundTask extends RemoteOSCTask {

  String filename;
  int sample_choice;

  //contructor loading the file
  public StopRemoteSoundTask (PApplet p, ControlP5 cp5, String id) {
    super(p, cp5, id);

    this.message = "/speaker/stop";
    this.filename = "example.wav";
    this.sample_choice = 0;
    //adding the sample_choice inside the address
    this.message = this.message + "/" + this.sample_choice;

    update_content();
  }

  //contructor loading the file
  public StopRemoteSoundTask (PApplet p, ControlP5 cp5, String id, String m, String f, int s, boolean repeat) {
	  super(p, cp5, id);
	  
	  this.message = m;
	  this.filename = f;
	  this.sample_choice = s;
	  //adding the sample_choice inside the address
	  this.message = this.message + "/" + this.sample_choice;
	  
	  this.repeat = repeat;
	  
	  update_content();
  }

  //duplicate method
  StopRemoteSoundTask clone_it () {
	  
    return new StopRemoteSoundTask(this.p, this.cp5, this.name, this.message, this.filename, this.sample_choice, this.repeat);
  }

  /*
  void update_name(String name) {
    this.filename = name;
    update_content();
  }
  */
  
  //adding the sample_choice inside the address
  void combine_message_and_sample() {
	  String temp = message.substring(0, message.length()-1);
	  this.message = temp + this.sample_choice;
  }
  
  //updates the sample_choice and the message content accordingly
  void update_sample(int new_sample) {
	  sample_choice = new_sample;
	  combine_message_and_sample();
	  update_content();
  }

  void update_content () {
	  //this.content = new Object[] {filename};
	  this.content = new Object[] {sample_choice};
  }


  //UI config
  Group load_gui_elements(State s) {
	  /*
    CallbackListener cb_enter = generate_callback_enter();
    //CallbackListener cb_leave = generate_callback_leave();

    String g_name = this.get_gui_id();
    
    String textlabel = "Stop audio";
    int font_size 	 = (int)(((ZenStates)p).get_font_size());
    int textwidth 	 = (int)((ZenStates)p).textWidth(textlabel);
    int backgroundheight = (int)(font_size* 7);


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

    /*
    cp5.addTextfield(g_name+ "/filename")
      .setPosition(localx, localy)
      .setSize(w, 15)
      .setGroup(g)
      .setAutoClear(false)
      .setLabel("filename")
      .setText(this.filename)
      .align(ControlP5.CENTER, ControlP5.CENTER,ControlP5.CENTER, ControlP5.CENTER)
      .onChange(cb_enter)
      .onReleaseOutside(cb_enter)
      .getCaptionLabel().align(ControlP5.CENTER, ControlP5.BOTTOM_OUTSIDE)
    ;

    create_gui_toggle(localx, localy+localoffset, w, g, cb_enter);
    */
    
	  Group g					= super.load_gui_elements(s);
	  CallbackListener cb_enter = generate_callback_enter();
	  String g_name			  	= this.get_gui_id();
	  int w 					= g.getWidth()-(localx*2);
	  
	  textlabel 	 			= "Stop audio";
	  backgroundheight 			= (int)(font_size* 7);
	  
	  g.setBackgroundHeight(backgroundheight);
	  g.setLabel(textlabel);
	  
    create_gui_toggle(localx, localy, w, g, cb_enter);

    cp5.addDropdownList(g_name+ "/sample")
    .setPosition(localx, localy+localoffset)
    .setSize(w, 150)
    .setGroup(g)
    .setLabel("sample")
    .align(ControlP5.CENTER, ControlP5.CENTER,ControlP5.CENTER, ControlP5.CENTER)
    //.setItemHeight(20)
    //.setBarHeight(15)
    .setItemHeight((int)(font_size*1.25))
    .setBarHeight((int)(font_size*1.25))
    .close()
    .onChange(cb_enter)
    //.onReleaseOutside(cb_enter)
    .addItem("Sample 1", 0)
    .addItem("Sample 2", 1)
    .addItem("Sample 3", 2)
    .addItem("Sample 4", 3)
    .setDefaultValue(this.sample_choice)
    .getCaptionLabel().align(ControlP5.CENTER, ControlP5.BOTTOM_OUTSIDE)
    ;

    return g;
  }


  CallbackListener generate_callback_enter() {
    return new CallbackListener() {
          public void controlEvent(CallbackEvent theEvent) {

            //if this group is not open, returns...
            if (!((Group)cp5.get(get_gui_id())).isOpen()) return;

            String s = theEvent.getController().getName();

            if (s.equals(get_gui_id() + "/sample")) 
            	update_sample((int)theEvent.getController().getValue());
            /*
            if (s.equals(get_gui_id() + "/filename")) {
                String newFilename = theEvent.getController().getValueLabel().getText();

                //if the filename is empty, resets
                if (newFilename.trim().equalsIgnoreCase("")) {
                  ((Textfield)cp5.get(get_gui_id() + "/filename")).setText(filename);
                  return;
                }

                update_name(newFilename);
            }
            */

            check_repeat_toggle(s, theEvent);
          }
    };
  }

  void reset_gui_fields() {
    String g_name = this.get_gui_id();
    String nv;

    //if this group is not open, returns...
    if (!((Group)cp5.get(get_gui_id())).isOpen()) return;

    /*
    nv = ((Textfield)cp5.get(g_name+"/filename")).getText();
    update_name(nv);
    */

  }




}
