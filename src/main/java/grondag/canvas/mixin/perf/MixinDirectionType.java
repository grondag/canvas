/*******************************************************************************
 * Copyright (C) 2018 grondag
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/

package grondag.canvas.mixin.perf;

import java.util.Iterator;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import com.google.common.collect.Iterators;

import grondag.canvas.helper.DirectionHelper;
import net.minecraft.util.math.Direction;

@Mixin(Direction.Type.class)
public abstract class MixinDirectionType {
    /**
     * @reason Use static array instance for iterator to avoid making garbage.
     * @author grondag
     */
    @Overwrite
    public Iterator<Direction> iterator() {
        switch ((Direction.Type) (Object) this) {
        case HORIZONTAL:
            return Iterators.<Direction>forArray(DirectionHelper.HORIZONTAL_FACES);

        case VERTICAL:
            return Iterators.<Direction>forArray(DirectionHelper.VERTICAL_FACES);

        default:
            throw new Error("Someone's been tampering with the universe!");
        }

    }
}