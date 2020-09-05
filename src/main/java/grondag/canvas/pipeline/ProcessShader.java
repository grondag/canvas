package grondag.canvas.pipeline;

import net.minecraft.util.Identifier;

import grondag.canvas.material.MaterialVertexFormats;
import grondag.canvas.shader.GlProgram;
import grondag.canvas.shader.GlProgram.Uniform1fImpl;
import grondag.canvas.shader.GlProgram.Uniform1iImpl;
import grondag.canvas.shader.GlProgram.Uniform2fImpl;
import grondag.canvas.shader.GlProgram.Uniform2iImpl;
import grondag.canvas.shader.GlShader;
import grondag.canvas.shader.GlShaderManager;
import grondag.canvas.shader.ShaderContext;
import grondag.frex.api.material.UniformRefreshFrequency;

public class ProcessShader {
	private GlProgram program;
	private Uniform2iImpl size;
	private Uniform2fImpl distance;
	private Uniform1iImpl lod;
	private Uniform1fImpl intensity;

	private final Identifier fragmentId;
	private final Identifier vertexId;
	private final String[] samplers;

	ProcessShader(Identifier vertexId, Identifier fragmentId, String... samplers) {
		this.fragmentId = fragmentId;
		this.vertexId = vertexId;
		this.samplers = samplers;
	}

	void unload() {
		if (program != null) {
			program.unload();
			program = null;
		}
	}

	public ProcessShader activate() {
		if (program == null) {
			final GlShader vs = GlShaderManager.INSTANCE.getOrCreateVertexShader(vertexId, ShaderContext.PROCESS);
			final GlShader fs = GlShaderManager.INSTANCE.getOrCreateFragmentShader(fragmentId, ShaderContext.PROCESS);
			program = new GlProgram(vs, fs, MaterialVertexFormats.PROCESS_VERTEX_UV, ShaderContext.PROCESS);
			size = (Uniform2iImpl) program.uniform2i("_cvu_size", UniformRefreshFrequency.ON_LOAD, u -> u.set(1, 1));
			lod = (Uniform1iImpl) program.uniform1i("_cvu_lod", UniformRefreshFrequency.ON_LOAD, u -> u.set(0));
			distance = (Uniform2fImpl) program.uniform2f("_cvu_distance", UniformRefreshFrequency.ON_LOAD, u -> u.set(0, 0));
			intensity = (Uniform1fImpl) program.uniform1f("cvu_intensity", UniformRefreshFrequency.ON_LOAD, u -> u.set(0));

			int tex = 0;

			for (final String samplerName : samplers) {
				final int n  = tex++;
				program.uniformSampler2d(samplerName, UniformRefreshFrequency.ON_LOAD, u -> u.set(n));
			}

			program.load();
		}

		program.activate();

		return this;
	}

	public ProcessShader size(int w, int h) {
		if  (program != null && GlProgram.activeProgram() == program) {
			size.set(w, h);
			size.upload();
		}

		return this;
	}

	public ProcessShader distance(float x, float y) {
		if  (program != null && GlProgram.activeProgram() == program) {
			distance.set(x, y);
			distance.upload();
		}

		return this;
	}

	public ProcessShader lod(int lod) {
		if  (program != null && GlProgram.activeProgram() == program) {
			this.lod.set(lod);
			this.lod.upload();
		}

		return this;
	}

	public ProcessShader intensity(float intensity) {
		if  (program != null && GlProgram.activeProgram() == program) {
			this.intensity.set(intensity);
			this.intensity.upload();
		}

		return this;
	}
}
