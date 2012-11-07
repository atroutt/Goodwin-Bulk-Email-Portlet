/*
Copyright (c) 2003-2010, CKSource - Frederico Knabben. All rights reserved.
For licensing, see LICENSE.html or http://ckeditor.com/license
*/

CKEDITOR.editorConfig = function( config )
{
		config.toolbar_Basic =
		[
			['Format','Font','FontSize'],
			['TextColor','BGColor'],
            ['Link','Unlink'],
			['Bold','Italic','Underline','Strike','-','NumberedList','BulletedList','-','Outdent','Indent'],
			['JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock'],
			['Cut','Copy','Paste','PasteText','PasteFromWord']
		] ;
		config.toolbar = 'Basic';
};
