package com.tac.guns.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;
import com.mojang.blaze3d.platform.InputConstants.Type;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ClientRegistry;
import org.lwjgl.glfw.GLFW;

import javax.swing.text.JTextComponent;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.function.Function;

/**
 * Helps to fix key conflict issue. Adapted solution from FMUM.
 *
 * @see InputHandler
 * @author Giant_Salted_Fish
 */
@OnlyIn( Dist.CLIENT )
public final class KeyBind
{
	public static final HashMap< String, KeyBind > REGISTRY = new HashMap<>();

	public boolean down = false;

	private Key keyCode;

	private final LinkedList< Runnable > keyPressCallbacks = new LinkedList<>();

	/**
	 * The corresponding vanilla key bind object. Its key bind will be set to
	 *  to avoid key conflict in game.
	 */
	private KeyMapping keyBind;

	/**
	 * Use {@link #KeyBind(String, String, int, Type...)} if you want to specify key category
	 *
	 * @param inputType Will be {@link Type#KEYSYM} if not present
	 */
	KeyBind( String name, int keyCode, Type... inputType ) {
		this( name, "key.categories.tac", keyCode, inputType );
	}

	/**
	 * @see #KeyBind(String, int, Type...)
	 */
	KeyBind( String name, String category, int keyCode, Type... inputType )
	{
		final boolean isValidKey = keyCode >= 0;
		if ( isValidKey )
		{
			final Type type = inputType.length > 0 ? inputType[ 0 ] : Type.KEYSYM;
			this.keyCode = type.getOrCreate( keyCode );
		}
		else this.keyCode = InputConstants.UNKNOWN;// INPUT_INVALID;

		final GunConflictContext conflictContext = GunConflictContext.IN_GAME_HOLDING_WEAPON;
		this.keyBind = new KeyMapping(name, conflictContext, this.keyCode, category );

		// Bind to none to avoid conflict
		this.keyBind.setKey( InputConstants.UNKNOWN );

		REGISTRY.put( this.name(), this );
	}

	public String name() { return this.keyBind.getName(); }

	public Key keyCode() { return this.keyCode; }

	/**
	 * Add a callback that will be invoked on the press of this key. It will only be invoked once
	 * until the key is released and pressed again.
	 */
	public void addPressCallback( Runnable callback ) { this.keyPressCallbacks.add( callback ); }

	void update( boolean down )
	{
		if ( down ^ this.down )
		{
			this.down = down;
			if ( down ) { this.keyPressCallbacks.forEach( Runnable::run ); }
		}
	}

	void inactiveUpdate( boolean down )
	{
		// Only handle key release if is inactive
		if( !down ) { this.down = down; }
	}

	void restoreKeyBind() { this.keyBind.setKey( this.keyCode ); }

	public void setKeyCode( Key keyCode ) { this.keyCode = keyCode; }
	boolean clearKeyBind()
	{
		final Key code = this.keyBind.getKey();
		if( code == this.keyCode ) { return false; }

		// Key bind has been changed, update it
		this.setKeyCode( code );
		this.keyBind.setKey( InputConstants.UNKNOWN );
		return true;
	}

	void regis() { ClientRegistry.registerKeyBinding( this.keyBind ); }
}


















