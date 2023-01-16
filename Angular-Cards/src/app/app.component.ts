import { Component, NgZone } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {

  public gifts: string[] = []

  public displayImage: boolean = false;
  public path: SafeResourceUrl | undefined;

  public location: string = ""

  constructor(private _sanitizer: DomSanitizer, private zone: NgZone) {

      (window as any).activity.onReceiveImage = (image: string) => {
        console.log("image is");
        console.log(image)

        this.zone.run(() => {
          this.path = this._sanitizer.bypassSecurityTrustResourceUrl(image);
          console.log(this.path)
          this.displayImage = true
        })
    }

    (window as any).activity.onReceiveLocation = (location: string) => {
      console.log("location");
      console.log(location)

      this.zone.run(() => {
        this.location = location
      })
  }

    this.readList();
  }

  ngOnInit(): void {

    let str = this.getActivity().getImageContent()
    if (str.length > 0) {
      this.path = this._sanitizer.bypassSecurityTrustResourceUrl(str);
      console.log(this.path)
      this.displayImage = true
    }

    this.location = this.getActivity().getLocation()
  }

  private readList() {
    let giftString = this.getActivity().receiveList();
    this.gifts = JSON.parse(!!giftString ? giftString: "[]");
  }

  public get giftList(): string[] {
    if (this.gifts.length > 3) {
       let list = this.gifts.slice(0, 3)
       console.log(list)
       list.push("To many gifts ....")
       return list
    }

    return this.gifts
  }

  public finish() {
      this.getActivity().done();
  }

  private getActivity(): Activity {
    if (!!(window as any).activity) {
      return (window as any).activity as Activity
    }

    let activity = new Activity()
    // setup methods
    return activity
  }
}

export class Activity {
  public receiveList: () => string = () => "[]"
  public done: () => void = () => {}
  public getImageContent: () => string = () => ""
  public getLocation: () => string = () => ""
}
