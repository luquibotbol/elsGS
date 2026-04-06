import {
  Component,
  OnInit,
  AfterViewInit,
  OnDestroy,
  ViewChild,
  ElementRef,
  NgZone,
  ChangeDetectorRef,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MutualFund } from './models/mutual-fund.model';
import { InvestmentResult } from './models/investment-result.model';
import { ApiService } from './services/api.service';

declare const THREE: any;

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.html',
  styleUrls: ['./app.css'],
})
export class AppComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('threeCanvas', { static: true }) canvasRef!: ElementRef<HTMLCanvasElement>;

  // Calculator state
  mutualFunds: MutualFund[] = [];
  selectedFund: MutualFund | null = null;
  principal: number | null = null;
  years: number | null = null;
  investmentResult: InvestmentResult | null = null;
  isLoading: boolean = false;
  isCalculating: boolean = false;
  errorMessage: string = '';

  // AI state
  activeAiTab: string = 'portfolio';
  aiLoading: boolean = false;

  // Portfolio Optimizer
  portfolioAmount: number = 0;
  portfolioRisk: string = 'moderate';
  portfolioResult: string = '';

  // AI Advisor Chat
  chatMessages: { role: string; content: string }[] = [];
  chatInput: string = '';

  // Risk Analysis
  analysisResult: string = '';

  // Fund Compare
  compareTickers: string[] = [];
  compareResult: string = '';

  private animationFrameId: number = 0;
  private renderer: any;
  private scene: any;
  private camera: any;
  private particles: any;
  private targetPositions: Float32Array = new Float32Array(0);
  private scatteredPositions: Float32Array = new Float32Array(0);
  private particleCount = 0;
  private morphProgress = 0;
  private morphDirection = 1;
  private clock: any;

  constructor(
    private apiService: ApiService,
    private ngZone: NgZone,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.fetchMutualFunds();
  }

  ngAfterViewInit() {
    this.ngZone.runOutsideAngular(() => {
      this.initThreeJS();
    });
  }

  ngOnDestroy() {
    if (this.animationFrameId) {
      cancelAnimationFrame(this.animationFrameId);
    }
    if (this.renderer) {
      this.renderer.dispose();
    }
    window.removeEventListener('resize', this.onResize);
  }

  // ── Three.js Setup ──

  private initThreeJS(): void {
    if (typeof THREE === 'undefined') return;

    const canvas = this.canvasRef.nativeElement;
    const width = window.innerWidth;
    const height = window.innerHeight;

    // Scene
    this.scene = new THREE.Scene();

    // Camera
    this.camera = new THREE.PerspectiveCamera(60, width / height, 0.1, 1000);
    this.camera.position.z = 8;

    // Renderer
    this.renderer = new THREE.WebGLRenderer({
      canvas,
      antialias: true,
      alpha: true,
    });
    this.renderer.setSize(width, height);
    this.renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
    this.renderer.setClearColor(0x000000, 0);

    // Clock
    this.clock = new THREE.Clock();

    // Generate GS particle positions
    this.generateGSParticles();

    // Create particle system
    this.createParticleSystem();

    // Resize listener
    window.addEventListener('resize', this.onResize);

    // Animate
    this.animate();
  }

  /**
   * Renders "G" and "S" separately onto hidden 2D canvases, then positions
   * the G on the left side and S on the right side of the screen so
   * the calculator in the center doesn't cover them.
   */
  private generateGSParticles(): void {
    const allPoints: { x: number; y: number }[] = [];

    // Helper: render a single letter and extract points with an X offset
    const sampleLetter = (letter: string, xOffset: number) => {
      const offCanvas = document.createElement('canvas');
      const ctx = offCanvas.getContext('2d')!;
      const canvasW = 256;
      const canvasH = 256;
      offCanvas.width = canvasW;
      offCanvas.height = canvasH;

      ctx.fillStyle = '#000';
      ctx.fillRect(0, 0, canvasW, canvasH);
      ctx.fillStyle = '#FFF';
      ctx.font = 'bold 200px IBM Plex Sans, Arial, sans-serif';
      ctx.textAlign = 'center';
      ctx.textBaseline = 'middle';
      ctx.fillText(letter, canvasW / 2, canvasH / 2);

      const imageData = ctx.getImageData(0, 0, canvasW, canvasH);
      const step = 3;

      for (let y = 0; y < canvasH; y += step) {
        for (let x = 0; x < canvasW; x += step) {
          const i = (y * canvasW + x) * 4;
          if (imageData.data[i] > 128) {
            allPoints.push({
              x: (x / canvasW - 0.5) * 4 + xOffset,  // 4 units wide, shifted by offset
              y: -(y / canvasH - 0.5) * 4,
            });
          }
        }
      }
    };

    // G on the left (-5.5), S on the right (+5.5)
    sampleLetter('G', -5.5);
    sampleLetter('S', 5.5);

    // Sample to target count
    const targetCount = Math.min(allPoints.length, 1200);
    const sampledPoints: { x: number; y: number }[] = [];

    if (allPoints.length <= targetCount) {
      sampledPoints.push(...allPoints);
    } else {
      const indices = new Set<number>();
      while (indices.size < targetCount) {
        indices.add(Math.floor(Math.random() * allPoints.length));
      }
      indices.forEach((i) => sampledPoints.push(allPoints[i]));
    }

    this.particleCount = sampledPoints.length;
    this.targetPositions = new Float32Array(this.particleCount * 3);
    this.scatteredPositions = new Float32Array(this.particleCount * 3);

    for (let i = 0; i < this.particleCount; i++) {
      // Target: GS letter shape (flat on Z with slight random depth)
      this.targetPositions[i * 3] = sampledPoints[i].x;
      this.targetPositions[i * 3 + 1] = sampledPoints[i].y;
      this.targetPositions[i * 3 + 2] = (Math.random() - 0.5) * 0.5;

      // Scattered: random sphere positions
      const theta = Math.random() * Math.PI * 2;
      const phi = Math.acos(2 * Math.random() - 1);
      const r = 3 + Math.random() * 4;
      this.scatteredPositions[i * 3] = r * Math.sin(phi) * Math.cos(theta);
      this.scatteredPositions[i * 3 + 1] = r * Math.sin(phi) * Math.sin(theta);
      this.scatteredPositions[i * 3 + 2] = r * Math.cos(phi);
    }
  }

  private createParticleSystem(): void {
    const geometry = new THREE.BufferGeometry();

    // Start from scattered positions
    const positions = new Float32Array(this.particleCount * 3);
    const sizes = new Float32Array(this.particleCount);
    const alphas = new Float32Array(this.particleCount);

    for (let i = 0; i < this.particleCount; i++) {
      positions[i * 3] = this.scatteredPositions[i * 3];
      positions[i * 3 + 1] = this.scatteredPositions[i * 3 + 1];
      positions[i * 3 + 2] = this.scatteredPositions[i * 3 + 2];
      sizes[i] = 1.5 + Math.random() * 2.5;
      alphas[i] = 0.4 + Math.random() * 0.6;
    }

    geometry.setAttribute('position', new THREE.BufferAttribute(positions, 3));
    geometry.setAttribute('size', new THREE.BufferAttribute(sizes, 1));
    geometry.setAttribute('alpha', new THREE.BufferAttribute(alphas, 1));

    // Custom shader material for GS blue glowing particles
    const material = new THREE.ShaderMaterial({
      uniforms: {
        uTime: { value: 0 },
        uColor1: { value: new THREE.Color(0x7AB0E0) },  // GS Blue light
        uColor2: { value: new THREE.Color(0x6699CC) },  // GS Blue
        uColor3: { value: new THREE.Color(0xFFFFFF) },  // White accent
      },
      vertexShader: `
        attribute float size;
        attribute float alpha;
        varying float vAlpha;
        varying float vSize;
        uniform float uTime;

        void main() {
          vAlpha = alpha;
          vSize = size;
          vec4 mvPosition = modelViewMatrix * vec4(position, 1.0);
          gl_PointSize = size * (200.0 / -mvPosition.z);
          gl_Position = projectionMatrix * mvPosition;
        }
      `,
      fragmentShader: `
        uniform float uTime;
        uniform vec3 uColor1;
        uniform vec3 uColor2;
        uniform vec3 uColor3;
        varying float vAlpha;
        varying float vSize;

        void main() {
          // Circular particle shape
          float dist = length(gl_PointCoord - vec2(0.5));
          if (dist > 0.5) discard;

          // Soft glow
          float strength = 1.0 - (dist * 2.0);
          strength = pow(strength, 1.5);

          // Mix gold colors with subtle navy highlights
          float colorMix = sin(uTime * 0.5 + vSize * 2.0) * 0.5 + 0.5;
          vec3 color = mix(uColor1, uColor2, colorMix);

          // Add subtle blue sparkle to some particles
          float blueMix = step(0.92, sin(uTime * 1.3 + vSize * 5.0) * 0.5 + 0.5);
          color = mix(color, uColor3, blueMix * 0.4);

          // Pulse alpha
          float pulseAlpha = vAlpha * (0.7 + 0.3 * sin(uTime * 1.5 + vSize * 3.0));

          gl_FragColor = vec4(color, strength * pulseAlpha);
        }
      `,
      transparent: true,
      depthWrite: false,
      blending: THREE.AdditiveBlending,
    });

    this.particles = new THREE.Points(geometry, material);
    this.scene.add(this.particles);
  }

  private animate = (): void => {
    this.animationFrameId = requestAnimationFrame(this.animate);

    const elapsed = this.clock.getElapsedTime();
    const positions = this.particles.geometry.attributes.position.array;

    // One-time sequence:
    //   0-2s:   particles scattered (initial state)
    //   2-4s:   gather into GS letters
    //   4-9s:   hold GS (5 seconds)
    //   9-11s:  scatter back out
    //   11s+:   stay as floating particles forever
    let morph: number;
    if (elapsed < 2) {
      // Start scattered
      morph = 0;
    } else if (elapsed < 4) {
      // Gather into GS
      morph = this.easeInOutCubic((elapsed - 2) / 2);
    } else if (elapsed < 9) {
      // Hold GS for 5 seconds
      morph = 1;
    } else if (elapsed < 11) {
      // Scatter back out
      morph = 1 - this.easeInOutCubic((elapsed - 9) / 2);
    } else {
      // Stay as particles forever
      morph = 0;
    }

    // Interpolate positions
    for (let i = 0; i < this.particleCount; i++) {
      const i3 = i * 3;
      // Add gentle per-particle oscillation when formed
      const wobble = morph * Math.sin(elapsed * 0.8 + i * 0.05) * 0.03;

      positions[i3] =
        this.scatteredPositions[i3] +
        (this.targetPositions[i3] - this.scatteredPositions[i3]) * morph +
        wobble;
      positions[i3 + 1] =
        this.scatteredPositions[i3 + 1] +
        (this.targetPositions[i3 + 1] - this.scatteredPositions[i3 + 1]) * morph +
        wobble;
      positions[i3 + 2] =
        this.scatteredPositions[i3 + 2] +
        (this.targetPositions[i3 + 2] - this.scatteredPositions[i3 + 2]) * morph;
    }

    this.particles.geometry.attributes.position.needsUpdate = true;

    // Update time uniform
    this.particles.material.uniforms.uTime.value = elapsed;

    // Slow Y rotation
    this.particles.rotation.y = Math.sin(elapsed * 0.1) * 0.08;

    this.renderer.render(this.scene, this.camera);
  };

  private easeInOutCubic(t: number): number {
    return t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2;
  }

  private onResize = (): void => {
    const w = window.innerWidth;
    const h = window.innerHeight;
    this.camera.aspect = w / h;
    this.camera.updateProjectionMatrix();
    this.renderer.setSize(w, h);
  };

  // ── Section Parsing + Markdown Rendering ──

  /**
   * Splits AI response into sections based on ## or ### headers.
   * Returns array of { title, body } for rendering as separate cards.
   */
  parseSections(text: string): { title: string; body: string }[] {
    if (!text) return [];
    // Split on lines starting with ## or ###
    const lines = text.split('\n');
    const sections: { title: string; body: string }[] = [];
    let currentTitle = '';
    let currentBody: string[] = [];

    for (const line of lines) {
      const headerMatch = line.match(/^#{1,3}\s+(.+)$/);
      if (headerMatch) {
        // Save previous section if it has content
        if (currentTitle || currentBody.length > 0) {
          sections.push({
            title: currentTitle,
            body: currentBody.join('\n').trim(),
          });
        }
        currentTitle = headerMatch[1];
        currentBody = [];
      } else {
        currentBody.push(line);
      }
    }
    // Push last section
    if (currentTitle || currentBody.length > 0) {
      sections.push({
        title: currentTitle,
        body: currentBody.join('\n').trim(),
      });
    }

    // If there's only 1 section with no title, return empty (use renderMarkdown instead)
    if (sections.length <= 1 && !sections[0]?.title) {
      return [];
    }
    return sections;
  }

  renderMarkdown(text: string): string {
    if (!text) return '';
    let html = text
      // Escape HTML
      .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
      // Bold: **text** or __text__
      .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
      .replace(/__(.+?)__/g, '<strong>$1</strong>')
      // Italic: *text* or _text_
      .replace(/\*(.+?)\*/g, '<em>$1</em>')
      // Headers: ### Header
      .replace(/^### (.+)$/gm, '<h4 class="md-h4">$1</h4>')
      .replace(/^## (.+)$/gm, '<h3 class="md-h3">$1</h3>')
      .replace(/^# (.+)$/gm, '<h2 class="md-h2">$1</h2>')
      // Horizontal rule
      .replace(/^---$/gm, '<hr class="md-hr">')
      // Bullet lists: - item or * item
      .replace(/^[\-\*] (.+)$/gm, '<li>$1</li>')
      // Numbered lists: 1. item
      .replace(/^\d+\. (.+)$/gm, '<li class="md-ol">$1</li>')
      // Inline code
      .replace(/`(.+?)`/g, '<code class="md-code">$1</code>')
      // Line breaks
      .replace(/\n/g, '<br>');

    // Wrap consecutive <li> in <ul>
    html = html.replace(/((?:<li>.*?<\/li><br>?)+)/g, '<ul class="md-ul">$1</ul>');
    html = html.replace(/((?:<li class="md-ol">.*?<\/li><br>?)+)/g, '<ol class="md-ol-list">$1</ol>');
    // Clean up extra <br> inside lists
    html = html.replace(/<\/li><br>/g, '</li>');

    return html;
  }

  // ── Number Formatting ──

  formatNumber(value: number | null): string {
    if (value === null || value === undefined || value === 0) return '';
    return value.toLocaleString('en-US');
  }

  onPrincipalInput(event: Event): void {
    const raw = (event.target as HTMLInputElement).value.replace(/,/g, '');
    this.principal = parseFloat(raw) || 0;
    // Update the display with commas
    (event.target as HTMLInputElement).value = this.formatNumber(this.principal);
  }

  onPortfolioAmountInput(event: Event): void {
    const raw = (event.target as HTMLInputElement).value.replace(/,/g, '');
    this.portfolioAmount = parseFloat(raw) || 0;
    (event.target as HTMLInputElement).value = this.formatNumber(this.portfolioAmount);
  }

  // ── API Methods ──

  fetchMutualFunds(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.apiService.getMutualFunds().subscribe({
      next: (funds: MutualFund[]) => {
        this.mutualFunds = funds;
        if (funds.length > 0) {
          this.selectedFund = funds[0];
        }
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Failed to fetch mutual funds.';
        this.isLoading = false;
        this.cdr.detectChanges();
      },
    });
  }

  calculateFutureValue(): void {
    if (!this.selectedFund || !this.principal || this.principal <= 0 || !this.years || this.years <= 0) {
      this.errorMessage = 'Please complete all fields with valid values.';
      return;
    }

    this.isCalculating = true;
    this.errorMessage = '';
    this.investmentResult = null;

    this.apiService
      .calculateFutureValue(this.selectedFund.ticker, this.principal!, this.years!)
      .subscribe({
        next: (result) => {
          this.investmentResult = result;
          this.errorMessage = '';
          this.isCalculating = false;
          this.cdr.detectChanges();
        },
        error: () => {
          this.errorMessage = 'Failed to calculate future value.';
          this.isCalculating = false;
          this.cdr.detectChanges();
        },
      });
  }

  // ── AI Methods ──

  generatePortfolio(): void {
    if (this.portfolioAmount <= 0) {
      this.errorMessage = 'Please enter a valid investment amount.';
      return;
    }
    this.aiLoading = true;
    this.portfolioResult = '';
    this.apiService.generatePortfolio(this.portfolioAmount, this.portfolioRisk).subscribe({
      next: (res) => {
        this.portfolioResult = res.content;
        this.aiLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Failed to generate portfolio. Check your API key.';
        this.aiLoading = false;
        this.cdr.detectChanges();
      },
    });
  }

  sendChatMessage(): void {
    const msg = this.chatInput.trim();
    if (!msg) return;

    this.chatMessages.push({ role: 'user', content: msg });
    this.chatInput = '';
    this.aiLoading = true;

    this.apiService.chatWithAdvisor(msg).subscribe({
      next: (res) => {
        this.chatMessages.push({ role: 'ai', content: res.content });
        this.aiLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.chatMessages.push({ role: 'ai', content: 'Sorry, I encountered an error. Please try again.' });
        this.aiLoading = false;
        this.cdr.detectChanges();
      },
    });
  }

  analyzeRisk(): void {
    if (!this.investmentResult) return;
    this.aiLoading = true;
    this.analysisResult = '';
    this.apiService.analyzeRisk(this.investmentResult).subscribe({
      next: (res) => {
        this.analysisResult = res.content;
        this.aiLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Failed to analyze risk. Check your API key.';
        this.aiLoading = false;
        this.cdr.detectChanges();
      },
    });
  }

  toggleCompareFund(ticker: string): void {
    const idx = this.compareTickers.indexOf(ticker);
    if (idx >= 0) {
      this.compareTickers.splice(idx, 1);
    } else if (this.compareTickers.length < 4) {
      this.compareTickers.push(ticker);
    }
  }

  compareFunds(): void {
    if (this.compareTickers.length < 2) return;
    this.aiLoading = true;
    this.compareResult = '';
    this.apiService.compareFunds(this.compareTickers).subscribe({
      next: (res) => {
        this.compareResult = res.content;
        this.aiLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Failed to compare funds. Check your API key.';
        this.aiLoading = false;
        this.cdr.detectChanges();
      },
    });
  }
}
