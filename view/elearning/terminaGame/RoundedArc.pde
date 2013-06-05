class RoundedArc{
	float  start;
	float  stop;
	float  r;
	float  rs;
	ResultGraph rg;
	Vertex first;
	Vertex last;
	float startMargin;
	float stopMargin;

	RoundedArc(float start, float stop, float r, float rs, ResultGraph rg){
		this.first = rg.vertices.get(0);
		this.last = rg.vertices.get(0);

		this.start = start;
		this.stop = stop;
		this.r = r;
		this.rs = rs;  
		this.rg = rg;
	}

	void display(){
		noStroke();
		fill(GREY);
		arc(tg.cx, tg.cy, 2 * (r + rs), 2 * (r + rs), start , stop );
		fill(BACKGROUND);
		ellipse(tg.cx,tg.cy,2 * (r - rs), 2 * (r - rs) );
		float startX = tg.cx + r * cos(start);
		float startY = tg.cy + r * sin(start);
		float stopX = tg.cx + r * cos(stop);
		float stopY = tg.cy + r * sin(stop);
		fill(GREY);
		ellipse(startX,startY, 2 * rs,2 * rs);
		ellipse(stopX,stopY, 2 * rs, 2 * rs);
	}


	void update(){
		outerRadius = rg.getBiggestCornerDistance(rg.ownTags);
		innerRadius = rg.getSmallestCornerDistance(rg.ownTags);
		rs = (outerRadius - innerRadius)/2;
		r = (outerRadius + innerRadius)/2;


		first = rg.ownTags.get(0);
		last = rg.ownTags.get(0);
		for(Vertex v: rg.ownTags){
			if (first.angle > v.angle ){
				first = v;
			}

			if(last.angle < v.angle){
				last = v;
			}
		}

		start = first.angle + startMargin;
		stop = last.angle + stopMargin;

		rs *= 1.1;
		if(start < 0){
			start += TWO_PI;
		}
		if(stop < 0){
			stop += TWO_PI;
		}
	}

	void shrinkExpand(){

		float mx = rg.cx + r * cos(start);
		float my = rg.cy + r * sin(start);
		float[] corners = first.getCorners();
		float[] leftCorners = new float[0];

		for(int i = 0; i < corners.length; i += 2){    
			float cox = corners[i];
			float coy = corners[i + 1];

			if(isLeft(rg.cx,rg.cy,mx,my,cox,coy)){
				leftCorners = append(leftCorners, cox);
				leftCorners = append(leftCorners, coy);  
			}
		}

		float d = 0;
		for(int i = 0; i < leftCorners.length; i +=2){
			float _d = dist(mx,my,leftCorners[i], leftCorners[i+1]);
			if(d < _d)
				d = _d;  
		}

		if(d < rs * 0.9){    //first is inside arc
			startMargin += 0.01;  
		} else if (d > rs){ //first debords
			startMargin -= 0.01;
		}

		mx = rg.cx + r * cos(stop);
		my = rg.cy + r * sin(stop);
		corners = last.getCorners();
		float[] rightCorners = new float[0];

		for(int i = 0; i < corners.length; i += 2){    
			float cox = corners[i];
			float coy = corners[i + 1];

			if(isRight(rg.cx,rg.cy,mx,my,cox,coy)){
				rightCorners = append(rightCorners, cox);
				rightCorners = append(rightCorners, coy);  
			}
		}

		float d = 0;
		for(int i = 0; i < rightCorners.length; i +=2){
			float _d = dist(mx,my,rightCorners[i], rightCorners[i+1]);
			if(d < _d)
				d = _d;  
		}
		if(d < rs * 0.9){    //last is inside arc
			stopMargin -= 0.01;  
		} else if (d > rs ){ //last debords
			stopMargin += 0.01;
		}

	}

	//if c is left of line ab
	boolean isRight(float ax, float ay, float bx, float by, float cx, float cy){
		return ((bx - ax)*(cy - ay) - (by - ay)*(cx - ax)) > 0;
	}

	boolean isLeft(float ax, float ay, float bx, float by, float cx, float cy){
		return !isRight(ax,ay,bx,by,cx,cy); 
	}  


}
