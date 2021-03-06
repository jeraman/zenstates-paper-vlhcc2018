

import processing.core.PApplet;

import java.util.UUID;
import java.util.Vector;

import controlP5.*;

public class StateMachine extends Task {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	State begin;
	//State end;
	State actual;
	Vector<State> states;
	String title; //this should be the name. the super name should be an id instead.

	float stateTimerMilestone = 0;
	float stateTimer          = 0;
	public boolean debug;
	boolean brandnew; //has the user added  any state or task added to this state machine?

	transient StateMachinePreview smp;

	//contructor
	public StateMachine (PApplet p, ControlP5 cp5, String name) {
		super (p, cp5, name);
		title   = name;
		//begin   = new State(p, cp5, "BEGIN_" + name);
		//end     = new State(p, cp5, "END_"+name);
		begin   = new State(p, cp5, "BEGIN");
		//end     = new State(p, cp5, "END");
		states  = new Vector<State>();
		debug = ZenStates.instance().debug();
		
		brandnew = true;

		actual = begin;

		//sets the global variables related to this blackboard
		//init_global_variables();

		if (debug)
			System.out.println("State_Machine " + this.name + " is inited!");
	}
	
	//contructor
	public StateMachine (PApplet p, ControlP5 cp5, String name, boolean repeat) {
		this(p, cp5, name);
		this.repeat = repeat;
	}


	/*
  //contructor
  public StateMachine (PApplet p, String name) {
    super (p, name);
    begin   = new State("BEGIN_" + name);
    end     = new State("END_"+name);
    states  = new Vector<State>();
    //preview = new State_Machine_Preview(this);

    actual = begin;

    //sets the global variables related to this blackboard
    init_global_variables();

    if (debug)  println("State_Machine " + this.name + " is inited!");
  }
	 */
	
	void check_if_any_substatemachine_needs_to_be_reloaded_from_file () {
		this.begin.check_if_any_substatemachine_needs_to_be_reloaded_from_file();
		//this.end.check_if_any_substatemachine_needs_to_be_reloaded_from_file();
		
		for (State s : states) 
			s.check_if_any_substatemachine_needs_to_be_reloaded_from_file();
		

	}
	
	void reload_from_file () {
		//checks if there is a file with the same name
		boolean there_is_file = ((ZenStates)p).serializer.check_if_file_exists_in_sketchpath(title);
		
		if (there_is_file) {
			//((ZenStates)p).is_loading = true;
			//((ZenStates)p).cp5.setAutoDraw(false);
			//load newtile from file
			StateMachine loaded = ((ZenStates)p).serializer.loadSubStateMachine(title);
			//next step is to copy all parameters of loaded to this state machine
			mirror(loaded);			
		}
	}

	void build (PApplet p, ControlP5 cp5) {
		this.p = p;
		this.cp5 = cp5;

		this.begin.build(p, cp5);
		//this.end.build(p, cp5);

		for (State s : states)
			s.build(p, cp5);

		//sets the global variables related to this blackboard
		//init_global_variables();

		//load_gui_elements();
	}
	
	StateMachine clone_state_machine_saved_in_file(String title) {
		return ((ZenStates)p).serializer.loadSubStateMachine(title);
	}
	
	StateMachine clone_state_machine_not_saved_in_file(String title) {
		StateMachine duplicate = null;
		
		//clone the state machine
		duplicate = new StateMachine(p, cp5, title, this.repeat);
		
		//mpving the begin state
		duplicate.begin= this.begin.clone_it();
		
		//clone the states
		for (State s: states)
			duplicate.add_state(s.clone_it());
		
		duplicate.hide();
		
		return duplicate;
	}
	
	StateMachine clone_it() {
		StateMachine duplicate = null;
		
		//if there is a file with this name, load it from file!
		if (((ZenStates)p).serializer.existsSubStateMachineInFile(title))
			duplicate = clone_state_machine_saved_in_file(title);
		else 
			duplicate = clone_state_machine_not_saved_in_file(title);
		
		return duplicate;
	}
	
	//makes the current statemachine to mirror another statemachine sm
	void mirror (StateMachine sm) {
		//state machine variables
		this.title    = sm.title;
		this.begin    = sm.begin;
		//this.end      = sm.end;
		this.states   = sm.states;
		this.brandnew = false;
		//task variables
		this.repeat   = sm.repeat;
		
		reinit_id_and_load_gui_internal_elements();	
	}
	
	
	//loads the ui of internal elements
	void reinit_id_and_load_gui_internal_elements () {
		this.begin.remove_all_gui_items();
		//this.end.remove_all_gui_items();
		for (State s : states) 
			s.remove_all_gui_items();
		
		this.begin.reinit_id();
		//this.end.reinit_id();
		for (State s : states) 
			s.reinit_id();
		
		this.begin.init_gui();
		//this.end.init_gui();
		for (State s : states) 
			s.init_gui();
		
		this.begin.hide_gui();
		//this.end.hide_gui();
		for (State s : states) 
			s.hide_gui();
	}
	
	
	StateMachine getReferenceForThisStateMachine() {
		return this;
	}

	//run all tasks associated to this node
	void run () {
		if (!should_run())
			return;
		
		//debug
		//if(name.contains("END"))
		//p.println("im executing " + title);

		this.status = Status.RUNNING;

		update_actual(begin);

		reset_state_timer();
		
		actual.run();
		
		if (debug)
			System.out.println("running the State_Machine " + this.title + ". actual is " + actual.get_name());
	}

	//stops all tasks associated to this node
	void stop() {
		super.stop();

		//stopping all states...
		for (State s : states) {
			s.reset_first_time();
			s.stop();
		}

		//stop begin and end
		begin.reset_first_time();
		begin.stop();

		//end.reset_first_time();
		//end.stop();

		//updating the actual
		//actual = begin;
		//update_actual(begin);
		update_actual(null);

		//resets the stateTimer for this state machine
		reset_state_timer();

		this.status = Status.INACTIVE;

		if (debug)
			System.out.println("stopping State_Machine" + this.name);
	}

	void clear() {
		this.stop();

		//stopping all states...
		for (State s : states) {
			s.clear();
			remove_state(s);
		}

		//stop begin and end
		begin.clear();
		//end.clear();
	}

	//stops all tasks associated to this node
	void interrupt() {
		//stopping all states...
		for (State s : states)
			s.interrupt();

		//stop begin and end
		begin.interrupt();
		//end.interrupt();

		//updating the actual
		//actual = begin;
		//update_actual(begin);
		update_actual(null);

		//resets the stateTimer for this state machine
		reset_state_timer();

		this.status = Status.DONE;
		if (debug)
			System.out.println("iterrupting State_Machine" + this.name);
	}

	void update_title(String newtitle) {		
		String n = get_formated_blackboard_title();
		((ZenStates)p).board.remove(n+"_timer");
		this.title = newtitle;
		//board.remove(this.title+"_stateTimer");
		//this.title = newtitle.replace(" ", "_");
		//init_global_variables();
	}

	void update_actual (State next) {
		actual.is_actual = false;

		//does nothing if the actuall will be null
		if (next==null) return;

		//updating the actual
		actual = next;

		actual.is_actual = true;
	}

	//updates the status of this state
	void update_status() {

		//if it' done, no point to execute this
		if (this.status == Status.DONE)  return;

		//updating the status of the actual
		actual.update_status();

		/*
		//if this state finished. test this condition, maybe you need to overload the comparison!
		if (actual==end) {
			//p.println("reached an end!");
			end.run();
			this.status = Status.DONE;
			if (debug)
				System.out.println("State_Machine " + this.name +  " has reached its end and has successfully executed!");
		}
		*/

		//if there are no states associated to this State_Machine
		if (states.size()==0 & begin.get_number_of_connections()==0) {
			this.status = Status.DONE;
			if (debug)
				System.out.println("State_Machine " + this.name +  " is empty! Done!");
		}

		//checks if currect actual has any empty transition
		//check_for_empty_transition();
	}

	//function called everytime there is a new input
	void tick() {

		//if not ready yet, returns...
		if (this.status==Status.INACTIVE || this.status==Status.DONE) {
			reset_state_timer(); //sets timer to zero
			return;
		}

		//stores the results for the state changing
		State next = null;

		//updates global variables in the blackboard
		update_global_variables();

		//updates the status of the hfsm
		update_status();

		//tries to update the next
		next = actual.tick();

		//if it really changed to another state
		if (next!=null && next!=actual) {
			//refreshing the stateTimer in the blackboard
			reset_state_timer();
			if (debug)
				System.out.println("changing to a different state. reset stateTimer.");

		} else {
			if (debug)
				System.out.println("changing to the same state. do not reset stateTimer.");
		}

		//in case next is not null, change state!
		if (next!=null)
			update_actual(next);

	}
	
	//in case there are statemachine inside this state, this machine should be saved to file
	void save() {
		//saving the current state machine
		((ZenStates)p).serializer._saveAs(title, this);
		
		//saving substatemachines inside all states...
		for (State s : states)
			s.save();

		//save substatemachines inside begin and end
		begin.save();
		//end.save();
	}
	
	boolean is_brandnew() {
		return brandnew;
	}

	//add a state s to this State_Machine
	void add_state(State s) {
		/*
    //check if there is already a state with the same name
    //State result = get_state_by_name(s.get_name());
    State result = get_state_by_id(s.get_id());

    //in case there isn't
    if (result==null) {
      states.addElement(s);
      System.out.println("State " + s.get_name() + " added to State_Machine " + this.name);
    } else {
      System.out.println("There is alrealdy a state with this same name. Please, pick another name!");
    }
		 */
		brandnew = false; //this sm is no longer brandnew
		states.addElement(s);
		System.out.println("State " + s.get_name() + " added to State_Machine " + this.name);
	}

	//remove a state s from this State_Machine
	void remove_state(State s) {
		if (states.contains(s)) {
			//remove all tasks associated with the deleted state
			s.remove_all_tasks();

			//remove all connection from this state
			s.remove_all_connections();

			//remove all connections to this state
			this.remove_all_connections_to_a_state(s);

			//ControlP5 cp5 = HFSMPrototype.instance().cp5();
			//removes its ui components
			cp5.remove(s.get_id()+"/label");
			cp5.remove(s.get_id()+"/acc");
			//remove the state fmor the list
			this.states.removeElement(s);
			
			//if you're removing this state
			if (s == actual) {
				//stops
				((ZenStates)p).canvas.button_stop();
				p.println("You're removing the state that is currently executing. Halting the state machine.");
			}
			
		} else
			System.out.println("Unable to remove state " + s.get_name() + " from State_Machine " + this.name);
	}

	//removes a state based on a certain x y position in the screen
	void remove_state(int x, int y) {
		//iterates over all states
		for (State s : states)
			//if it intersects with a certain x y position
			if (s.intersects_gui(x, y)) {
				//removes this state
				this.remove_state(s);
				//and breaks (one remotion per time))
				return;
			}
	}

	void remove_all_connections_to_a_state (State dest) {
		if (debug)
			System.out.println("removing all connections to " + dest.toString());

		//removing all connection to a state in the begin and in the end
		this.begin.remove_all_connections_to_a_state(dest);
		//this.end.remove_all_connections_to_a_state(dest);

		//iterates over all states
		for (State s : states)
			s.remove_all_connections_to_a_state(dest);
	}
	
	void update_all_connections_to_a_state (State dest, String newid) {
		if (debug)
			System.out.println("updating all connections to " + dest.toString());
		
		//updating all connection to a state in the begin and in the end
		this.begin.update_all_connections_to_a_state(dest, newid);
		//this.end.update_all_connections_to_a_state(dest, newid);

		//iterates over all states
		for (State s : states)
			s.update_all_connections_to_a_state(dest, newid);
	}
	
	

	/*

  //returns a state by its name. returns null if not available
  State get_state_by_name(String name) {
      State result = null;

      if (this.begin.get_name().equalsIgnoreCase(name)) result=this.begin;
      if (this.end.get_name().equalsIgnoreCase(name))   result=this.end;

      //iterates over all states
      for (State s : states)
        if (s.get_name().equalsIgnoreCase(name)) result=s;

      if (result!=null)
        System.out.println("found! " + result.toString());
      else
        System.out.println("problem!");

      //returns the proper result
      return result;
  }

	 */

	//returns a state by its unique id. returns null if not available
	State get_state_by_id(String id) {
		State result = null;

		if (this.begin.get_id().equalsIgnoreCase(id)) result=this.begin;
		//if (this.end.get_id().equalsIgnoreCase(id))   result=this.end;

		//iterates over all states
		for (State s : states)
			if (s.get_id().equalsIgnoreCase(id)) result=s;

		if (result!=null)
			System.out.println("found! " + result.toString());
		else
			System.out.println("problem!");

		//returns the proper result
		return result;
	}

	//add a task t to the initialization of this State_Machine
	void add_initialization_task (Task t) {
		begin.add_task(t);
		System.out.println("Task " + t.name + " added to the initialization of State_Machine " + this.name);
	}

	//remove a task t to the initialization of this State_Machine
	void remove_initialization_task (Task t) {
		begin.remove_task(t);
		System.out.println("Task " + t.name + " removed from the initialization of State_Machine " + this.name);
	}

	/*
	//add a task t to the initialization of this State_Machine
	void add_finalization_task (Task t) {
		end.add_task(t);
		System.out.println("Task " + t.name + " added to the finalization of State_Machine " + this.name);
	}

	//remove a task t to the initialization of this State_Machine
	void remove_finalization_task (Task t) {
		end.remove_task(t);
		System.out.println("Task " + t.name + " removed from the finalization of State_Machine " + this.name);
	}
	*/

	//formats the title for the blackboard
	String get_formated_blackboard_title () {
		String n = this.title.replace(".", "_");
		n = n.replace(" ", "_");
		return n;
	}

	//inits the global variables related to this blackboard
	void init_global_variables() {
		String n = get_formated_blackboard_title();
		ZenStates.instance().board.put(n+"_timer", 0);
	}
	
	//updates the global variable related to this blackboard
	void update_global_variables() {
		update_state_timer();
		String n = get_formated_blackboard_title();
		ZenStates.instance().board.put(n+"_timer", this.stateTimer);
		//println("update variable " + this.stateTimer);
	}

	//updates the stateTimer variable related to this state machine
	void update_state_timer() {
		//if the PApplet wasn't loaded yet
		if (p==null) return;
		this.stateTimer = ((float)p.millis()/1000f)-stateTimerMilestone;
	}

	//resets the stateTimer variable related to this state machine
	void reset_state_timer() {
		//if the PApplet wasn't loaded yet
		if (p==null) return;

		this.stateTimerMilestone = (float)p.millis()/1000f;
		this.stateTimer          = 0;
		update_global_variables();
	}

	//returns how many states we have in this state machine
	int get_states_size() {
		return this.states.size();
	}

	/*******************************************
	 ** GUI FUNCTIONS ***************************
	 ********************************************/

	//draws all states associated with this state_machine
	void draw() {
		//if the Papplet wasn't loaded yet
		if (p==null) return;
		//else
		//p.println(p.millis());

		update_gui();

		//drawing the entry state
		begin.draw();
		begin.draw_begin();
		//drawing the states begining to this state machine
		for (State s : states)
			s.draw();
		//drawing the end state
		//end.draw();
		//end.draw_end();

		//drawing all pie menus  in a layer
		//draw_pie_menus();

		//drawing the actual, if running
		if (this.status==Status.RUNNING)
			actual.draw_actual();
	}

	void draw_pie_menus () {
		//drawing the entry state
		begin.draw_pie();
		//drawing the states begining to this state machine
		for (State s : states)
			s.draw();
		//drawing the end state
		//end.draw_pie();
	}

	void update_gui () {
		//verifies if user wants to create a new connection for this state
		update_state_connections_on_gui();
	}

	void hide() {
		begin.hide_gui();
		for (State s : states)
			s.hide_gui();
		//end.hide_gui();
	}

	void show() {
		begin.show_gui();
		for (State s : states)
			s.show_gui();
		//end.show_gui();
	}

	//returns a state that intersect test_x, test_y positions
	State intersects_gui(int test_x, int test_y) {
		State result = null;

		//println("testing intersection... " + test_x + " " + test_y);

		//testing the begin & end states
		if (this.begin.intersects_gui(test_x, test_y))  return this.begin;
		//if (this.end.intersects_gui(test_x, test_y))    return this.end;

		//iterates over the remaining states
		for (State s : states)
			//if intersects...
			if (s.intersects_gui(test_x, test_y)) {
				System.out.println("i found someone to be intersected");
				//updates the result
				result = s;
				break;
			}

		return result;
	}

	//reinit any name the user was trying to change it
	void reset_all_names_gui() {
		//resets the begin and the end states
		this.begin.reset_name();
		//this.end.reset_name();

		//iterates over the remaining states
		for (State s : states)
			//reinit the name in case the user was trying to change it
			s.reset_name();
	}

	CallbackListener generate_callback_enter() {
		return new CallbackListener() {
			public void controlEvent(CallbackEvent theEvent) {

				//if this group is not open, returns...
				if (!((Group)cp5.get(get_gui_id())).isOpen()) return;

				String s = theEvent.getController().getName();
				//System.out.println(s + " was entered");
				

				if (s.equals(get_gui_id() + "/name")) {
					String text = theEvent.getController().getValueLabel().getText();
					if (text.trim().equals("")) {
						text="(choose a name)";
						((Textfield)cp5.get(get_gui_id()+ "/name")).setText(text);
					}
					
					//if the name didn't change, return
					if(text.equals(title)) return;
					
					//update_title(text);
					process_title(text, title);
				}

				check_repeat_toggle(s, theEvent);
			}
			
			public void process_title(String newtitle, String oldtitle) {
				//does it finish with .zen extension?
				if (newtitle.endsWith(".zen")) { //if yes
					
					//checks if there is a file named newtitle
					boolean there_is_newtitle = ((ZenStates)p).serializer.check_if_file_exists_in_sketchpath(newtitle);
					
					//if there is a file named newtitle
					if (there_is_newtitle) {
						
						//if the curring machine is brandnew
						if (brandnew) {
							
							//p.print("we jsut loaded a sm from file! name: " + loaded.title);
							((ZenStates)p).is_loading = true;
							((ZenStates)p).cp5.setAutoDraw(false);
							//load newtile from file
							StateMachine loaded = ((ZenStates)p).serializer.loadSubStateMachine(newtitle);
							//next step is to copy all parameters of loaded to this state machine
							mirror(loaded);
							((ZenStates)p).is_loading = false;
							((ZenStates)p).cp5.setAutoDraw(true);
						
						//if the current machine isn't brandnew
						} else {
							
							//p.println("the submachine isn't brandnew and there is already a file using this name!");
							//remove the .zen extension
							newtitle = newtitle.replace(".zen", "");
							//update textfield on the ui
							((Textfield)cp5.get(get_gui_id()+ "/name")).setText(newtitle);
							//update title
							update_title(newtitle);
						}
					
					//if there is no file named newtitle
					} else {		  
						p.println("no " + newtitle + " was found in sketchpath");
						//delete the old
						((ZenStates)p).serializer.delete(oldtitle);
						//update title
						update_title(newtitle);
						//save new
						((ZenStates)p).serializer._saveAs(newtitle, getReferenceForThisStateMachine());
					}
					
				//if it does not finish with .zen, just update the name
				} else
					update_title(newtitle);
					
			}
		};
	}

	CallbackListener generate_callback_open_substate() {
		return new CallbackListener() {
			public void controlEvent(CallbackEvent theEvent) {

				String s = theEvent.getController().getName();
				System.out.println("open substate " + s);
				smp.open();
			}
		};
	}

	void close () {
		smp.close();
	}

	Group load_gui_elements(State s) {
		/*
		//creating the callbacks
		CallbackListener cb_enter = generate_callback_enter();
		CallbackListener cb_pressed = generate_callback_open_substate();
		//CallbackListener cb_leave = generate_callback_leave();
		//ControlP5 cp5 = HFSMPrototype.instance().cp5();

		//String g_name = s.get_name() + " " + this.get_name();
		//this.set_gui_id(g_name);
		String g_name = get_gui_id();
		
	    String textlabel = "State Machine";
	    int font_size 	 = (int)(((ZenStates)p).get_font_size());
	    int textwidth 	 = (int)((ZenStates)p).textWidth(textlabel);
	    //this background is more complex to define because it has a constant value in the middle (the state machine preview)
	    int backgroundheight = (int)(font_size* 17.5);
	    //int backgroundheight = (int)(font_size* 10.5)+75;


		int c1 = p.color(255, 50);
		int c2 = p.color(255, 25);

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
		  CallbackListener cb_pressed = generate_callback_open_substate();
		  String g_name			  	= this.get_gui_id();
		  int w 					= g.getWidth()-(localx*2);
		  
		  textlabel 	 			= "State Machine";
		  backgroundheight 			= (int)(font_size* 17.5);
		    
		  g.setBackgroundHeight(backgroundheight);
		  g.setLabel(textlabel);

		cp5.addTextfield(g_name+"/name")
		.setPosition(localx, localy)
		.setSize(w, (int)(font_size*1.25))
		.setGroup(g)
		.setAutoClear(false)
		.setLabel("name")
		.setText(this.title+"")
		.onChange(cb_enter)
		.onReleaseOutside(cb_enter)
		.getCaptionLabel().align(ControlP5.CENTER, ControlP5.BOTTOM_OUTSIDE);
		;

		smp = new StateMachinePreview( (ZenStates)p, this, localx, localy+localoffset);
		g.addCanvas((controlP5.Canvas)smp);

		//int preview_height = font_size*5;
		cp5.addButton(g_name+"/open_preview")
		//.setPosition(localx, localy+preview_height+localoffset)
		.setPosition(localx, localy+(int)(3.3*localoffset))
		.setSize(w, (int)(font_size*1.25))
		.setValue(0)
		.setLabel("open preview")
		.onPress(cb_pressed)
		.setGroup(g)
		;

		//create_gui_toggle(localx, localy+preview_height+(2*localoffset), w, g, cb_enter);
		create_gui_toggle(localx, localy+(int)(4.3*localoffset), w, g, cb_enter);

		return g;
	}

	void connect_state_if_demanded_by_user (State s) {
		//println("verify: mouse is hiting a new state or not?");

		//unfreezes state s
		s.unfreeze_movement_and_untrigger_connection();

		State intersected = this.intersects_gui(p.mouseX, p.mouseY);
		//if there is someone to connect to
		if (intersected!=null) {
			//connects
			s.connect(new Expression ("true"), intersected);
			//s.connect_anything_else_to_self();
			brandnew = false; //this statemachine is no longer brandnew
		}
	}

	//verifies on the gui if the user wants to create a new connection
	void update_state_connections_on_gui () {

		//updates the begin state
		if (this.begin.verify_if_user_released_mouse_while_temporary_connecting())
			connect_state_if_demanded_by_user(this.begin);

		//updates the begin state
		//if (this.end.verify_if_user_released_mouse_while_temporary_connecting())
		//	connect_state_if_demanded_by_user(this.end);

		//iterates over the remaining states
		for (State s : states)
			//if the mouse was released and there is a temporary connection on gui
			if (s.verify_if_user_released_mouse_while_temporary_connecting()) {
				connect_state_if_demanded_by_user(s);
				break;
			}

		//updates the last mouse position
		//lastMousePressed = mousePressed;
	}

	void remove_all_gui_connections_to_a_state (State dest) {

		//removing all connection to a state in the begin and in the end
		this.begin.remove_all_gui_connections_to_a_state(dest);
		//this.end.remove_all_gui_connections_to_a_state(dest);

		//iterates over all states
		for (State s : states)
			s.remove_all_gui_connections_to_a_state(dest);
	}

	void init_all_gui_connections_to_a_state (State dest) {

		//removing all connection to a state in the begin and in the end
		this.begin.init_all_gui_connections_to_a_state(dest);
		//this.end.init_all_gui_connections_to_a_state(dest);

		//iterates over all states
		for (State s : states)
			s.init_all_gui_connections_to_a_state(dest);
	}

	void reset_gui_fields() {
		String g_name = this.get_gui_id();
		String nv;

		//if this group is not open, returns...
		if (!((Group)cp5.get(get_gui_id())).isOpen()) return;

		//nothing in here!
	}

}
