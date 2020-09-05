package grondag.canvas.apiimpl.rendercontext;

import java.util.Random;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Matrix4f;

import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;

import grondag.canvas.CanvasMod;
import grondag.canvas.Configurator;
import grondag.canvas.apiimpl.material.MeshMaterialLayer;
import grondag.canvas.apiimpl.material.MeshMaterialLocator;
import grondag.canvas.apiimpl.mesh.MutableQuadViewImpl;
import grondag.canvas.buffer.encoding.VertexCollectorList;
import grondag.canvas.buffer.encoding.VertexEncoders;
import grondag.canvas.light.AoCalculator;
import grondag.canvas.material.EncodingContext;
import grondag.canvas.material.MaterialVertexFormats;
import grondag.canvas.mixinterface.Matrix3fExt;
import grondag.canvas.texture.SpriteInfoTexture;
import grondag.frex.api.material.MaterialMap;

public abstract class AbstractRenderContext implements RenderContext {
	public final float[] vecData = new float[3];
	public final int[] appendData  = new int[MaterialVertexFormats.MAX_QUAD_INT_STRIDE];
	public final VertexCollectorList collectors = new VertexCollectorList();
	private final ObjectArrayList<QuadTransform> transformStack = new ObjectArrayList<>();
	private static final QuadTransform NO_TRANSFORM = (q) -> true;
	private static final MaterialMap defaultMap = MaterialMap.defaultMaterialMap();

	protected Matrix4f matrix;
	protected Matrix3fExt normalMatrix;
	protected int overlay;
	protected MaterialMap materialMap = defaultMap;
	protected boolean isFluidModel = false;

	protected final String name;

	protected AbstractRenderContext(String name) {
		this.name = name;

		if (Configurator.enableLifeCycleDebug) {
			CanvasMod.LOG.info("Lifecycle Event: create render context " + name);
		}
	}

	public void close() {
		if (Configurator.enableLifeCycleDebug) {
			CanvasMod.LOG.info("Lifecycle Event: close render context " + name);
		}
	}

	private final QuadTransform stackTransform = (q) -> {
		int i = transformStack.size() - 1;

		while (i >= 0) {
			if (!transformStack.get(i--).transform(q)) {
				return false;
			}
		}

		return true;
	};

	private QuadTransform activeTransform = NO_TRANSFORM;

	protected final boolean transform(MutableQuadViewImpl q) {
		return activeTransform.transform(q);
	}

	protected boolean hasTransform() {
		return activeTransform != NO_TRANSFORM;
	}

	void mapMaterials(MutableQuadViewImpl quad) {
		if (isFluidModel || materialMap == defaultMap) {
			return;
		}

		final Sprite sprite = materialMap.needsSprite() ? SpriteInfoTexture.instance().fromId(quad.spriteId(0)) : null;
		final RenderMaterial mapped = materialMap.getMapped(sprite);

		if (mapped != null) {
			quad.material(mapped);
		}
	}

	@Override
	public void pushTransform(QuadTransform transform) {
		if (transform == null) {
			throw new NullPointerException("Renderer received null QuadTransform.");
		}

		transformStack.push(transform);

		if (transformStack.size() == 1) {
			activeTransform = transform;
		} else if (transformStack.size() == 2) {
			activeTransform = stackTransform;
		}
	}

	@Override
	public void popTransform() {
		transformStack.pop();

		if (transformStack.size() == 0) {
			activeTransform = NO_TRANSFORM;
		} else if (transformStack.size() == 1) {
			activeTransform = transformStack.get(0);
		}
	}

	protected final MeshConsumer meshConsumer = new MeshConsumer(this);
	protected final MutableQuadViewImpl makerQuad = meshConsumer.editorQuad;

	@Override
	public final Consumer<Mesh> meshConsumer() {
		return meshConsumer;
	}

	protected final FallbackConsumer fallbackConsumer = new FallbackConsumer(this);

	@Override
	public final Consumer<BakedModel> fallbackConsumer() {
		return fallbackConsumer;
	}

	@Override
	public final QuadEmitter getEmitter() {
		return meshConsumer.getEmitter();
	}

	// for use by fallback consumer
	protected boolean cullTest(int faceIndex) {
		return true;
	}

	protected final boolean cullTest(MutableQuadViewImpl quad) {
		return cullTest(quad.cullFaceId());
	}

	protected abstract Random random();

	public abstract boolean defaultAo();

	protected abstract BlockState blockState();

	public abstract EncodingContext materialContext();

	public abstract VertexConsumer consumer(MeshMaterialLayer mat);

	public abstract int indexedColor(int colorIndex);

	/**
	 * Used in contexts with a fixed brightness, like ITEM.
	 */
	public abstract int brightness();

	/**
	 * Null in some contexts, like ITEM.
	 */
	public abstract @Nullable AoCalculator aoCalc();

	public abstract int flatBrightness(MutableQuadViewImpl quad);

	public final int overlay() {
		return overlay;
	}

	public final Matrix4f matrix() {
		return matrix;
	}

	public final Matrix3fExt normalMatrix() {
		return normalMatrix;
	}

	protected abstract int defaultBlendModeIndex();

	public final void renderQuad() {
		final MutableQuadViewImpl quad = makerQuad;

		mapMaterials(quad);

		if (transform(quad) && cullTest(quad)) {
			final MeshMaterialLocator mat = quad.material().withDefaultBlendMode(defaultBlendModeIndex());
			quad.material(mat);
			VertexEncoders.get(materialContext(), mat).encodeQuad(quad, this);
		}
	}
}
