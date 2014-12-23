create table if not exists feed (
	_id integer primary key autoincrement,
	url varchar(512) not null,
	homepage varchar(512),
	type varchar(4), -- rss|rdf|atom
	title varchar(255),
	refreshDate integer,
	orderno int,
	enabled bit,
	description text,
	image_url varchar(512),
	color int
);
-- break
create table if not exists item (
	_id integer primary key autoincrement,
	feed_id integer references feed(_id) on delete cascade,
	
	url varchar(512) not null unique on conflict ignore,
	guid varchar(127),

	original_source varchar(512),
	original_author varchar(128),
	 
	title varchar(512) not null,
	author varchar(128),
	pub_date integer,
	update_date integer,
	description text,
	raw_content text,
		 
	image_url varchar(512),
	
	favorite bit,
	read bit
);
-- break
create table if not exists theme (
	_id integer primary key autoincrement,
    
    name varchar(255) not null unique on conflict ignore,
    
    roundedCorners bit,
    backgroundColor integer,
    backgroundOpacity integer,
    
    storyTitleColor integer,
    storyTitleFontSize integer,
    storyTitleUppercase bit,
    storyTitleMaxLines integer,
    storyTitleHide bit,
    
    storyDescriptionColor integer,
    storyDescriptionFontSize integer,
    storyDescriptionMaxWordCount integer,
    
    showFooter bit,
    footerUppercase bit,
    footerFontSize integer,
    storyAuthorColor integer,
    storyDateColor integer,
    
    thumbnailSize integer, 
    
    showWidgetTitle bit,
    widgetTitleColor integer,
    
    layout bit,
    numColumns integer default(1),
    
    dateFormat varchar(40)
);
-- break
create table if not exists behaviour (
	_id integer primary key autoincrement,
	
	name varchar(255) not null unique on conflict ignore,
	
	mobilizer varchar(20) default 'None',
	use_builtin_browser bit default 1,
	max_story_number integer,
	force_feed_as_author bit,
	lookup_thumbnail_in_body bit,
	hide_read_stories bit,
	
	clear_before_load bit,
	distribute_evenly bit
);
-- break
create table if not exists config (
	_id integer primary key autoincrement,
	widget_id integer not null,
	theme_id integer references theme(_id),
	behaviour_id integer references behaviour(_id),
	widget_title varchar(255)
);
-- break
create table if not exists config_feed (
	_id integer primary key autoincrement,
	config_id integer not null references config(_id) on delete cascade,
	feed_id integer not null references feed(_id) on delete cascade
);
-- break